package toubani.badr.transactionservice.dto;

import java.util.UUID;

public record AccountDto(UUID id, String customerReference, String status, String currency, double balance) {
}
