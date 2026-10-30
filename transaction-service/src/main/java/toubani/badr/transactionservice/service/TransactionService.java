package toubani.badr.transactionservice.service;

import toubani.badr.transactionservice.dto.CreateTransactionRequest;
import toubani.badr.transactionservice.dto.TransactionResponse;
import toubani.badr.transactionservice.dto.TransferRequest;
import toubani.badr.transactionservice.dto.TransferResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createPayment(CreateTransactionRequest request);

    TransactionResponse createDeposit(CreateTransactionRequest request);

    TransferResponse transfer(TransferRequest request);

    List<TransactionResponse> findAll();

    TransactionResponse findById(UUID id);

    List<TransactionResponse> findByAccount(UUID accountId);

    /** Accepte un UUID ou une référence client (utile pour les tools IA). */
    UUID resolveAccount(String reference);

    BigDecimal getMaxAmount();
}
