package toubani.badr.transactionservice.dto;

import toubani.badr.transactionservice.domain.Transaction;
import toubani.badr.transactionservice.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String merchantCategory,
        String country,
        Instant createdAt,
        TransactionType type,
        UUID transferId,
        UUID counterpartyAccountId) {

    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAccountId(), t.getAmount(), t.getCurrency(),
                t.getMerchantCategory(), t.getCountry(), t.getCreatedAt(), t.getType(), t.getTransferId(),
                t.getCounterpartyAccountId());
    }
}
