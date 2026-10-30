package toubani.badr.accountservice.service;

import toubani.badr.accountservice.dto.AccountResponse;
import toubani.badr.accountservice.dto.BalanceResponse;
import toubani.badr.accountservice.dto.CreateAccountRequest;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountResponse create(CreateAccountRequest request);

    List<AccountResponse> findAll();

    AccountResponse findById(UUID id);

    AccountResponse findByCustomerReference(String customerReference);

    /** Accepte un UUID ou une référence client (utile pour les tools IA). */
    AccountResponse resolve(String reference);

    AccountResponse block(UUID id);

    AccountResponse activate(UUID id);

    BalanceResponse getBalance(UUID id);

    BalanceResponse debit(UUID id, double amount);

    BalanceResponse credit(UUID id, double amount);
}
