package toubani.badr.transactionservice.web.graphql;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import toubani.badr.transactionservice.dto.CreateTransactionRequest;
import toubani.badr.transactionservice.dto.TransactionResponse;
import toubani.badr.transactionservice.dto.TransferRequest;
import toubani.badr.transactionservice.dto.TransferResponse;
import toubani.badr.transactionservice.service.TransactionService;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TransactionGraphQLController {

    private final TransactionService transactionService;

    @QueryMapping
    public List<TransactionResponse> transactions() {
        return transactionService.findAll();
    }

    @QueryMapping
    public TransactionResponse transactionById(@Argument UUID id) {
        return transactionService.findById(id);
    }

    @QueryMapping
    public List<TransactionResponse> transactionsByAccount(@Argument UUID accountId) {
        return transactionService.findByAccount(accountId);
    }

    @MutationMapping
    public TransactionResponse createTransaction(@Argument CreateTransactionRequest input) {
        return transactionService.createPayment(input);
    }

    @MutationMapping
    public TransferResponse transfer(@Argument TransferRequest input) {
        return transactionService.transfer(input);
    }
}
