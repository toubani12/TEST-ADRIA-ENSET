package toubani.badr.transactionservice.client;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import toubani.badr.transactionservice.dto.AccountDto;
import toubani.badr.transactionservice.dto.AmountRequest;
import toubani.badr.transactionservice.dto.BalanceDto;
import toubani.badr.transactionservice.exception.AccountServiceUnavailableException;

import java.util.UUID;

/**
 * Fallback Resilience4J : une erreur fonctionnelle (4xx, ex. compte introuvable) est propagée telle quelle,
 * une panne technique (timeout, service arrêté, circuit ouvert) devient AccountServiceUnavailableException (503).
 * Aucun solde n'est inventé : l'opération est refusée.
 */
@Component
@Slf4j
public class AccountClientFallbackFactory implements FallbackFactory<AccountClient> {

    @Override
    public AccountClient create(Throwable cause) {
        return new AccountClient() {
            private RuntimeException fail() {
                if (cause instanceof FeignException fe && fe.status() >= 400 && fe.status() < 500) {
                    return fe;
                }
                log.warn("Fallback account-service : {}", cause.toString());
                return new AccountServiceUnavailableException("account-service indisponible, réessayez plus tard");
            }

            @Override public AccountDto getAccount(UUID id) { throw fail(); }
            @Override public AccountDto getAccountByReference(String reference) { throw fail(); }
            @Override public BalanceDto getBalance(UUID id) { throw fail(); }
            @Override public BalanceDto debit(UUID id, AmountRequest request) { throw fail(); }
            @Override public BalanceDto credit(UUID id, AmountRequest request) { throw fail(); }
        };
    }
}
