package toubani.badr.transactionservice.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import toubani.badr.transactionservice.dto.CreateTransactionRequest;
import toubani.badr.transactionservice.dto.TransactionResponse;
import toubani.badr.transactionservice.dto.TransferRequest;
import toubani.badr.transactionservice.dto.TransferResponse;
import toubani.badr.transactionservice.service.TransactionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionMcpTools {

    private final TransactionService transactionService;

    @Tool(name = "listTransactions", description = "Liste les transactions, les plus récentes d'abord, éventuellement filtrées par compte")
    public List<TransactionResponse> listTransactions(
            @ToolParam(description = "UUID du compte ou référence client (ex. CUST-001) ; vide pour toutes", required = false) String account,
            @ToolParam(description = "Nombre maximum de transactions retournées", required = false) Integer limit) {
        List<TransactionResponse> transactions = account == null || account.isBlank()
                ? transactionService.findAll()
                : transactionService.findByAccount(transactionService.resolveAccount(account));
        return limit == null ? transactions : transactions.stream().limit(limit).toList();
    }

    @Tool(name = "getTransaction", description = "Retourne le détail d'une transaction")
    public TransactionResponse getTransaction(@ToolParam(description = "UUID de la transaction") String id) {
        return transactionService.findById(UUID.fromString(id.trim()));
    }

    @Tool(name = "createPayment", description = "Enregistre un paiement (débit) sur un compte")
    public TransactionResponse createPayment(
            @ToolParam(description = "UUID du compte ou référence client (ex. CUST-001)") String account,
            @ToolParam(description = "Montant strictement positif") BigDecimal amount,
            @ToolParam(description = "Devise ISO 4217, identique à celle du compte (ex. EUR)") String currency,
            @ToolParam(description = "Catégorie du commerçant (ex. GROCERY, TRAVEL)") String merchantCategory,
            @ToolParam(description = "Pays ISO 3166 alpha-2 (ex. FR, MA)") String country) {
        return transactionService.createPayment(new CreateTransactionRequest(
                transactionService.resolveAccount(account), amount, currency, merchantCategory, country));
    }

    @Tool(name = "deposit", description = "Enregistre un dépôt (crédit) sur un compte")
    public TransactionResponse deposit(
            @ToolParam(description = "UUID du compte ou référence client (ex. CUST-001)") String account,
            @ToolParam(description = "Montant strictement positif") BigDecimal amount,
            @ToolParam(description = "Devise ISO 4217, identique à celle du compte (ex. EUR)") String currency) {
        return transactionService.createDeposit(new CreateTransactionRequest(
                transactionService.resolveAccount(account), amount, currency, "DEPOSIT", null));
    }

    @Tool(name = "transfer", description = "Effectue un virement d'un compte vers un autre")
    public TransferResponse transfer(
            @ToolParam(description = "Compte source : UUID ou référence client") String fromAccount,
            @ToolParam(description = "Compte cible : UUID ou référence client") String toAccount,
            @ToolParam(description = "Montant strictement positif") BigDecimal amount,
            @ToolParam(description = "Devise ISO 4217 des deux comptes (ex. EUR)") String currency) {
        return transactionService.transfer(new TransferRequest(transactionService.resolveAccount(fromAccount),
                transactionService.resolveAccount(toAccount), amount, currency));
    }

    @Tool(name = "getMaxAmount", description = "Retourne le plafond autorisé pour une transaction")
    public BigDecimal getMaxAmount() {
        return transactionService.getMaxAmount();
    }
}
