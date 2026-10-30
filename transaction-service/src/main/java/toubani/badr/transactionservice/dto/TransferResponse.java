package toubani.badr.transactionservice.dto;

import java.util.UUID;

public record TransferResponse(UUID transferId, TransactionResponse debit, TransactionResponse credit) {
}
