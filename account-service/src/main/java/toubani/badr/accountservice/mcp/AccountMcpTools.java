package toubani.badr.accountservice.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import toubani.badr.accountservice.dto.AccountResponse;
import toubani.badr.accountservice.dto.BalanceResponse;
import toubani.badr.accountservice.dto.CreateAccountRequest;
import toubani.badr.accountservice.service.AccountService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountMcpTools {

    private final AccountService accountService;

    @Tool(name = "listAccounts", description = "Liste tous les comptes bancaires (référence client, statut, devise, solde)")
    public List<AccountResponse> listAccounts() {
        return accountService.findAll();
    }

    @Tool(name = "getAccount", description = "Retourne le détail d'un compte bancaire")
    public AccountResponse getAccount(
            @ToolParam(description = "Identifiant UUID du compte ou référence client (ex. CUST-001)") String account) {
        return accountService.resolve(account);
    }

    @Tool(name = "getBalance", description = "Retourne le solde, la devise et le statut d'un compte")
    public BalanceResponse getBalance(
            @ToolParam(description = "Identifiant UUID du compte ou référence client (ex. CUST-001)") String account) {
        return accountService.getBalance(accountService.resolve(account).id());
    }

    @Tool(name = "createAccount", description = "Crée un nouveau compte bancaire actif")
    public AccountResponse createAccount(
            @ToolParam(description = "Référence client unique (ex. CUST-010)") String customerReference,
            @ToolParam(description = "Devise ISO 4217 en 3 lettres majuscules (ex. EUR, MAD)") String currency,
            @ToolParam(description = "Solde initial, positif ou nul") double initialBalance) {
        return accountService.create(new CreateAccountRequest(customerReference, currency, initialBalance));
    }

    @Tool(name = "blockAccount", description = "Bloque un compte bancaire (plus aucune opération possible)")
    public AccountResponse blockAccount(
            @ToolParam(description = "Identifiant UUID du compte ou référence client (ex. CUST-001)") String account) {
        return accountService.block(accountService.resolve(account).id());
    }

    @Tool(name = "activateAccount", description = "Réactive un compte bancaire bloqué")
    public AccountResponse activateAccount(
            @ToolParam(description = "Identifiant UUID du compte ou référence client (ex. CUST-001)") String account) {
        return accountService.activate(accountService.resolve(account).id());
    }
}
