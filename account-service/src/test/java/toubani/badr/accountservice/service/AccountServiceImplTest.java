package toubani.badr.accountservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import toubani.badr.accountservice.domain.AccountStatus;
import toubani.badr.accountservice.dto.AccountResponse;
import toubani.badr.accountservice.dto.CreateAccountRequest;
import toubani.badr.accountservice.exception.AccountBlockedException;
import toubani.badr.accountservice.exception.AccountNotFoundException;
import toubani.badr.accountservice.exception.DuplicateCustomerReferenceException;
import toubani.badr.accountservice.exception.InsufficientBalanceException;
import toubani.badr.accountservice.repository.AccountRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceImplTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void cleanUp() {
        accountRepository.deleteAll();
    }

    @Test
    void createAccountIsActiveByDefault() {
        AccountResponse account = accountService.create(new CreateAccountRequest("CUST-100", "EUR", 100));

        assertThat(account.id()).isNotNull();
        assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.balance()).isEqualTo(100);
        assertThat(account.createdAt()).isNotNull();
    }

    @Test
    void customerReferenceMustBeUnique() {
        accountService.create(new CreateAccountRequest("CUST-100", "EUR", 100));

        assertThatThrownBy(() -> accountService.create(new CreateAccountRequest("CUST-100", "EUR", 50)))
                .isInstanceOf(DuplicateCustomerReferenceException.class);
    }

    @Test
    void currencyAndInitialBalanceAreValidated() {
        assertThatThrownBy(() -> accountService.create(new CreateAccountRequest("CUST-101", "eur", 10)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> accountService.create(new CreateAccountRequest("CUST-102", "EUR", -1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debitAndCreditUpdateBalance() {
        UUID id = accountService.create(new CreateAccountRequest("CUST-100", "EUR", 100)).id();

        assertThat(accountService.debit(id, 30).balance()).isEqualTo(70);
        assertThat(accountService.credit(id, 50).balance()).isEqualTo(120);
        assertThat(accountService.getBalance(id).balance()).isEqualTo(120);
    }

    @Test
    void debitRefusedWhenBalanceInsufficient() {
        UUID id = accountService.create(new CreateAccountRequest("CUST-100", "EUR", 100)).id();

        assertThatThrownBy(() -> accountService.debit(id, 150)).isInstanceOf(InsufficientBalanceException.class);
        assertThat(accountService.getBalance(id).balance()).isEqualTo(100);
    }

    @Test
    void operationsRefusedOnBlockedAccount() {
        UUID id = accountService.create(new CreateAccountRequest("CUST-100", "EUR", 100)).id();
        accountService.block(id);

        assertThatThrownBy(() -> accountService.debit(id, 10)).isInstanceOf(AccountBlockedException.class);
        assertThatThrownBy(() -> accountService.credit(id, 10)).isInstanceOf(AccountBlockedException.class);

        accountService.activate(id);
        assertThat(accountService.credit(id, 10).balance()).isEqualTo(110);
    }

    @Test
    void amountMustBeStrictlyPositive() {
        UUID id = accountService.create(new CreateAccountRequest("CUST-100", "EUR", 100)).id();

        assertThatThrownBy(() -> accountService.debit(id, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> accountService.credit(id, -5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resolveAcceptsUuidOrCustomerReference() {
        UUID id = accountService.create(new CreateAccountRequest("CUST-100", "EUR", 100)).id();

        assertThat(accountService.resolve(id.toString()).customerReference()).isEqualTo("CUST-100");
        assertThat(accountService.resolve("CUST-100").id()).isEqualTo(id);
        assertThatThrownBy(() -> accountService.resolve("CUST-999")).isInstanceOf(AccountNotFoundException.class);
    }
}
