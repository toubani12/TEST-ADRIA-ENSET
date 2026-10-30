package toubani.badr.transactionservice.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import toubani.badr.transactionservice.dto.CreateTransactionRequest;
import toubani.badr.transactionservice.dto.TransactionResponse;
import toubani.badr.transactionservice.dto.TransferRequest;
import toubani.badr.transactionservice.dto.TransferResponse;
import toubani.badr.transactionservice.service.TransactionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionRestController {

    private final TransactionService transactionService;

    @GetMapping
    public List<TransactionResponse> findAll() {
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    public TransactionResponse findById(@PathVariable UUID id) {
        return transactionService.findById(id);
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionResponse> findByAccount(@PathVariable UUID accountId) {
        return transactionService.findByAccount(accountId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createPayment(@Valid @RequestBody CreateTransactionRequest request) {
        return transactionService.createPayment(request);
    }

    @PostMapping("/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createDeposit(@Valid @RequestBody CreateTransactionRequest request) {
        return transactionService.createDeposit(request);
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }
}
