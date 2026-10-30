package toubani.badr.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import toubani.badr.transactionservice.dto.AccountDto;
import toubani.badr.transactionservice.dto.AmountRequest;
import toubani.badr.transactionservice.dto.BalanceDto;

import java.util.UUID;

@FeignClient(name = "account-service", fallbackFactory = AccountClientFallbackFactory.class)
public interface AccountClient {

    @GetMapping("/api/accounts/{id}")
    AccountDto getAccount(@PathVariable UUID id);

    @GetMapping("/api/accounts/by-reference/{reference}")
    AccountDto getAccountByReference(@PathVariable String reference);

    @GetMapping("/api/accounts/{id}/balance")
    BalanceDto getBalance(@PathVariable UUID id);

    @PostMapping("/api/accounts/{id}/debit")
    BalanceDto debit(@PathVariable UUID id, @RequestBody AmountRequest request);

    @PostMapping("/api/accounts/{id}/credit")
    BalanceDto credit(@PathVariable UUID id, @RequestBody AmountRequest request);
}
