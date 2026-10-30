package toubani.badr.transactionservice.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toubani.badr.transactionservice.client.AccountClient;
import toubani.badr.transactionservice.domain.Transaction;
import toubani.badr.transactionservice.domain.TransactionType;
import toubani.badr.transactionservice.dto.*;
import toubani.badr.transactionservice.exception.*;
import toubani.badr.transactionservice.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@Transactional
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final BigDecimal maxAmount;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountClient accountClient,
                                  @Value("${bank.transaction.max-amount}") BigDecimal maxAmount) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.maxAmount = maxAmount;
    }

    @Override
    public TransactionResponse createPayment(CreateTransactionRequest request) {
        checkAmount(request.amount());
        BalanceDto balance = checkAccount(request.accountId(), request.currency());
        checkBalance(balance, request.amount());

        Transaction saved = transactionRepository.save(newTransaction(request, TransactionType.PAYMENT));
        callAccount(() -> accountClient.debit(request.accountId(), new AmountRequest(request.amount().doubleValue())));
        log.info("Paiement {} de {} {} sur le compte {}", saved.getId(), saved.getAmount(), saved.getCurrency(),
                saved.getAccountId());
        return TransactionResponse.from(saved);
    }

    @Override
    public TransactionResponse createDeposit(CreateTransactionRequest request) {
        checkAmount(request.amount());
        checkAccount(request.accountId(), request.currency());

        Transaction saved = transactionRepository.save(newTransaction(request, TransactionType.DEPOSIT));
        callAccount(() -> accountClient.credit(request.accountId(), new AmountRequest(request.amount().doubleValue())));
        log.info("Dépôt {} de {} {} sur le compte {}", saved.getId(), saved.getAmount(), saved.getCurrency(),
                saved.getAccountId());
        return TransactionResponse.from(saved);
    }

    @Override
    public TransferResponse transfer(TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new InvalidAmountException("Le compte source et le compte cible doivent être différents");
        }
        checkAmount(request.amount());
        BalanceDto source = checkAccount(request.fromAccountId(), request.currency());
        checkAccount(request.toAccountId(), request.currency());
        checkBalance(source, request.amount());

        UUID transferId = UUID.randomUUID();
        Transaction debit = transactionRepository.save(Transaction.builder()
                .accountId(request.fromAccountId()).amount(request.amount()).currency(request.currency())
                .merchantCategory("TRANSFER").type(TransactionType.TRANSFER_OUT)
                .transferId(transferId).counterpartyAccountId(request.toAccountId()).build());
        Transaction credit = transactionRepository.save(Transaction.builder()
                .accountId(request.toAccountId()).amount(request.amount()).currency(request.currency())
                .merchantCategory("TRANSFER").type(TransactionType.TRANSFER_IN)
                .transferId(transferId).counterpartyAccountId(request.fromAccountId()).build());

        AmountRequest amount = new AmountRequest(request.amount().doubleValue());
        callAccount(() -> accountClient.debit(request.fromAccountId(), amount));
        try {
            callAccount(() -> accountClient.credit(request.toAccountId(), amount));
        } catch (RuntimeException creditFailure) {
            // Compensation : on rend le montant au compte source, la transaction locale est annulée (rollback)
            log.warn("Crédit du virement {} échoué, compensation du compte {}", transferId, request.fromAccountId());
            callAccount(() -> accountClient.credit(request.fromAccountId(), amount));
            throw creditFailure;
        }
        log.info("Virement {} de {} {} : {} -> {}", transferId, request.amount(), request.currency(),
                request.fromAccountId(), request.toAccountId());
        return new TransferResponse(transferId, TransactionResponse.from(debit), TransactionResponse.from(credit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream().map(TransactionResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse findById(UUID id) {
        return transactionRepository.findById(id).map(TransactionResponse::from)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction introuvable : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> findByAccount(UUID accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(TransactionResponse::from).toList();
    }

    @Override
    public UUID resolveAccount(String reference) {
        String ref = reference == null ? "" : reference.trim();
        try {
            return callAccount(() -> accountClient.getAccount(UUID.fromString(ref))).id();
        } catch (IllegalArgumentException notAUuid) {
            return callAccount(() -> accountClient.getAccountByReference(ref)).id();
        }
    }

    @Override
    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    private void checkAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Le montant doit être strictement positif");
        }
        if (amount.compareTo(maxAmount) > 0) {
            throw new AmountLimitExceededException("Le montant " + amount + " dépasse le plafond autorisé de " + maxAmount);
        }
    }

    private BalanceDto checkAccount(UUID accountId, String currency) {
        BalanceDto balance = callAccount(() -> accountClient.getBalance(accountId));
        if (!balance.isActive()) {
            throw new AccountBlockedException("Le compte " + accountId + " n'est pas actif");
        }
        if (!balance.currency().equals(currency)) {
            throw new CurrencyMismatchException("La devise " + currency + " ne correspond pas à celle du compte ("
                    + balance.currency() + ")");
        }
        return balance;
    }

    private void checkBalance(BalanceDto balance, BigDecimal amount) {
        if (BigDecimal.valueOf(balance.balance()).compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Solde insuffisant : " + balance.balance() + " "
                    + balance.currency() + " disponible");
        }
    }

    private Transaction newTransaction(CreateTransactionRequest request, TransactionType type) {
        return Transaction.builder()
                .accountId(request.accountId())
                .amount(request.amount())
                .currency(request.currency())
                .merchantCategory(request.merchantCategory())
                .country(request.country())
                .type(type)
                .build();
    }

    private <T> T callAccount(Supplier<T> call) {
        try {
            return call.get();
        } catch (FeignException.NotFound e) {
            throw new AccountNotFoundException("Compte introuvable");
        } catch (FeignException e) {
            throw new AccountServiceUnavailableException("account-service indisponible ou a refusé l'opération : "
                    + e.status());
        }
    }
}
