package toubani.badr.accountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toubani.badr.accountservice.domain.Account;
import toubani.badr.accountservice.domain.AccountStatus;
import toubani.badr.accountservice.dto.AccountResponse;
import toubani.badr.accountservice.dto.BalanceResponse;
import toubani.badr.accountservice.dto.CreateAccountRequest;
import toubani.badr.accountservice.exception.AccountBlockedException;
import toubani.badr.accountservice.exception.AccountNotFoundException;
import toubani.badr.accountservice.exception.DuplicateCustomerReferenceException;
import toubani.badr.accountservice.exception.InsufficientBalanceException;
import toubani.badr.accountservice.repository.AccountRepository;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private static final Pattern CURRENCY = Pattern.compile("[A-Z]{3}");

    private final AccountRepository accountRepository;

    @Override
    public AccountResponse create(CreateAccountRequest request) {
        String reference = request.customerReference() == null ? "" : request.customerReference().trim();
        if (reference.isEmpty()) {
            throw new IllegalArgumentException("La référence client est obligatoire");
        }
        if (request.currency() == null || !CURRENCY.matcher(request.currency()).matches()) {
            throw new IllegalArgumentException("La devise doit être un code ISO 4217 (3 lettres majuscules)");
        }
        if (request.initialBalance() < 0) {
            throw new IllegalArgumentException("Le solde initial doit être positif ou nul");
        }
        if (accountRepository.existsByCustomerReference(reference)) {
            throw new DuplicateCustomerReferenceException("La référence client existe déjà : " + reference);
        }
        Account account = Account.builder()
                .customerReference(reference)
                .currency(request.currency())
                .balance(request.initialBalance())
                .status(AccountStatus.ACTIVE)
                .build();
        Account saved = accountRepository.save(account);
        log.info("Compte créé : {} ({})", saved.getCustomerReference(), saved.getId());
        return AccountResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream().map(AccountResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse findById(UUID id) {
        return AccountResponse.from(getAccount(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse findByCustomerReference(String customerReference) {
        return accountRepository.findByCustomerReference(customerReference)
                .map(AccountResponse::from)
                .orElseThrow(() -> new AccountNotFoundException(customerReference));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse resolve(String reference) {
        String ref = reference == null ? "" : reference.trim();
        try {
            return findById(UUID.fromString(ref));
        } catch (IllegalArgumentException notAUuid) {
            return findByCustomerReference(ref);
        }
    }

    @Override
    public AccountResponse block(UUID id) {
        Account account = getAccount(id);
        account.setStatus(AccountStatus.BLOCKED);
        log.info("Compte bloqué : {}", id);
        return AccountResponse.from(account);
    }

    @Override
    public AccountResponse activate(UUID id) {
        Account account = getAccount(id);
        account.setStatus(AccountStatus.ACTIVE);
        log.info("Compte activé : {}", id);
        return AccountResponse.from(account);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID id) {
        return BalanceResponse.from(getAccount(id));
    }

    @Override
    public BalanceResponse debit(UUID id, double amount) {
        Account account = getActiveAccount(id);
        checkAmount(amount);
        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException("Solde insuffisant : " + account.getBalance() + " "
                    + account.getCurrency() + " disponible pour un débit de " + amount);
        }
        account.debit(amount);
        log.info("Débit de {} sur le compte {}", amount, id);
        return BalanceResponse.from(account);
    }

    @Override
    public BalanceResponse credit(UUID id, double amount) {
        Account account = getActiveAccount(id);
        checkAmount(amount);
        account.credit(amount);
        log.info("Crédit de {} sur le compte {}", amount, id);
        return BalanceResponse.from(account);
    }

    private Account getAccount(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id.toString()));
    }

    private Account getActiveAccount(UUID id) {
        Account account = getAccount(id);
        if (!account.isActive()) {
            throw new AccountBlockedException("Le compte " + id + " est bloqué");
        }
        return account;
    }

    private void checkAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif");
        }
    }
}
