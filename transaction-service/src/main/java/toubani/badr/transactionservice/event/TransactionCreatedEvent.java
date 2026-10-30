package toubani.badr.transactionservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Événement publié dans bank.transactions.created après la persistance d'une transaction. */
public record TransactionCreatedEvent(UUID eventId, String eventType, UUID transactionId, UUID accountId,
                                      BigDecimal amount, String currency, Instant occurredAt) {

    public static TransactionCreatedEvent of(UUID transactionId, UUID accountId, BigDecimal amount, String currency,
                                             Instant occurredAt) {
        return new TransactionCreatedEvent(UUID.randomUUID(), "TransactionCreated", transactionId, accountId,
                amount, currency, occurredAt == null ? Instant.now() : occurredAt);
    }

    public String toJson() {
        return "{\"eventId\":\"" + eventId + "\",\"eventType\":\"" + eventType + "\",\"transactionId\":\""
                + transactionId + "\",\"accountId\":\"" + accountId + "\",\"amount\":" + amount.toPlainString()
                + ",\"currency\":\"" + currency + "\",\"occurredAt\":\"" + occurredAt + "\"}";
    }
}
