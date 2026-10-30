package toubani.badr.transactionservice.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import toubani.badr.transactionservice.client.AccountClient;
import toubani.badr.transactionservice.domain.Transaction;
import toubani.badr.transactionservice.dto.*;
import toubani.badr.transactionservice.exception.*;
import toubani.badr.transactionservice.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountClient accountClient;

    private TransactionServiceImpl service;

    private final UUID accountId = UUID.randomUUID();
    private final UUID otherAccountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(transactionRepository, accountClient, new BigDecimal("1000"));
        lenient().when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CreateTransactionRequest payment(String amount, String currency) {
        return new CreateTransactionRequest(accountId, new BigDecimal(amount), currency, "GROCERY", "FR");
    }

    private void account(UUID id, double balance, String currency, String status) {
        when(accountClient.getBalance(id)).thenReturn(new BalanceDto(id, balance, currency, status));
    }

    @Test
    void paymentDebitsAccount() {
        account(accountId, 500, "EUR", "ACTIVE");

        TransactionResponse response = service.createPayment(payment("250", "EUR"));

        assertThat(response.amount()).isEqualByComparingTo("250");
        verify(accountClient).debit(accountId, new AmountRequest(250));
    }

    @Test
    void amountMustBeStrictlyPositive() {
        assertThatThrownBy(() -> service.createPayment(payment("0", "EUR"))).isInstanceOf(InvalidAmountException.class);
        verifyNoInteractions(accountClient);
    }

    @Test
    void amountAboveLimitIsRefused() {
        assertThatThrownBy(() -> service.createPayment(payment("1500", "EUR")))
                .isInstanceOf(AmountLimitExceededException.class);
        verifyNoInteractions(accountClient);
    }

    @Test
    void unknownAccountIsRefused() {
        when(accountClient.getBalance(accountId)).thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> service.createPayment(payment("10", "EUR")))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void blockedAccountIsRefused() {
        account(accountId, 500, "EUR", "BLOCKED");

        assertThatThrownBy(() -> service.createPayment(payment("10", "EUR")))
                .isInstanceOf(AccountBlockedException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void currencyMismatchIsRefused() {
        account(accountId, 500, "EUR", "ACTIVE");

        assertThatThrownBy(() -> service.createPayment(payment("10", "MAD")))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void insufficientBalanceIsRefused() {
        account(accountId, 50, "EUR", "ACTIVE");

        assertThatThrownBy(() -> service.createPayment(payment("100", "EUR")))
                .isInstanceOf(InsufficientBalanceException.class);
        verify(accountClient, never()).debit(any(), any());
    }

    @Test
    void depositCreditsAccountWithoutBalanceCheck() {
        account(accountId, 0, "EUR", "ACTIVE");

        service.createDeposit(payment("300", "EUR"));

        verify(accountClient).credit(accountId, new AmountRequest(300));
    }

    @Test
    void transferDebitsSourceAndCreditsTarget() {
        account(accountId, 500, "EUR", "ACTIVE");
        account(otherAccountId, 10, "EUR", "ACTIVE");

        TransferResponse response = service.transfer(
                new TransferRequest(accountId, otherAccountId, new BigDecimal("200"), "EUR"));

        assertThat(response.debit().transferId()).isEqualTo(response.transferId());
        assertThat(response.credit().accountId()).isEqualTo(otherAccountId);
        verify(accountClient).debit(accountId, new AmountRequest(200));
        verify(accountClient).credit(otherAccountId, new AmountRequest(200));
    }

    @Test
    void transferToSameAccountIsRefused() {
        assertThatThrownBy(() -> service.transfer(
                new TransferRequest(accountId, accountId, new BigDecimal("10"), "EUR")))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void transferIsCompensatedWhenCreditFails() {
        account(accountId, 500, "EUR", "ACTIVE");
        account(otherAccountId, 10, "EUR", "ACTIVE");
        when(accountClient.credit(eq(otherAccountId), any())).thenThrow(FeignException.ServiceUnavailable.class);

        assertThatThrownBy(() -> service.transfer(
                new TransferRequest(accountId, otherAccountId, new BigDecimal("200"), "EUR")))
                .isInstanceOf(AccountServiceUnavailableException.class);
        verify(accountClient).debit(accountId, new AmountRequest(200));
        verify(accountClient).credit(accountId, new AmountRequest(200));
    }
}
