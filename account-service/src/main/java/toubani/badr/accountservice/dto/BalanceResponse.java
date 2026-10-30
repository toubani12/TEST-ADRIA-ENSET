package toubani.badr.accountservice.dto;

import toubani.badr.accountservice.domain.Account;
import toubani.badr.accountservice.domain.AccountStatus;

import java.util.UUID;

public record BalanceResponse(UUID accountId, double balance, String currency, AccountStatus status) {

    public static BalanceResponse from(Account account) {
        return new BalanceResponse(account.getId(), account.getBalance(), account.getCurrency(), account.getStatus());
    }
}
