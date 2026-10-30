package toubani.badr.accountservice.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import toubani.badr.accountservice.dto.AccountResponse;
import toubani.badr.accountservice.dto.AmountRequest;
import toubani.badr.accountservice.dto.BalanceResponse;
import toubani.badr.accountservice.dto.CreateAccountRequest;
import toubani.badr.accountservice.service.AccountService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountRestController {

    private final AccountService accountService;

    @GetMapping
    public List<AccountResponse> findAll() {
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    public AccountResponse findById(@PathVariable UUID id) {
        return accountService.findById(id);
    }

    @GetMapping("/by-reference/{reference}")
    public AccountResponse findByReference(@PathVariable String reference) {
        return accountService.findByCustomerReference(reference);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(request);
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse balance(@PathVariable UUID id) {
        return accountService.getBalance(id);
    }

    @PostMapping("/{id}/debit")
    public BalanceResponse debit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return accountService.debit(id, request.amount());
    }

    @PostMapping("/{id}/credit")
    public BalanceResponse credit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return accountService.credit(id, request.amount());
    }

    @PutMapping("/{id}/block")
    public AccountResponse block(@PathVariable UUID id) {
        return accountService.block(id);
    }

    @PutMapping("/{id}/activate")
    public AccountResponse activate(@PathVariable UUID id) {
        return accountService.activate(id);
    }
}
