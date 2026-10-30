package toubani.badr.transactionservice.dto;

import java.util.UUID;

public record BalanceDto(UUID accountId, double balance, String currency, String status) {

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
