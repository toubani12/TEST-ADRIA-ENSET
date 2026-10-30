package toubani.badr.accountservice.dto;

import toubani.badr.accountservice.domain.Account;
import toubani.badr.accountservice.domain.AccountStatus;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String customerReference,
        AccountStatus status,
        String currency,
        double balance,
        Instant createdAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getCustomerReference(), account.getStatus(),
                account.getCurrency(), account.getBalance(), account.getCreatedAt());
    }
}
