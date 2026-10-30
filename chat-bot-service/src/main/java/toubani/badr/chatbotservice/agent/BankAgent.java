package toubani.badr.chatbotservice.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Agent bancaire : le LLM (via une simple clé d'API, aucun modèle entraîné localement) orchestre
 * les tools MCP exposés par account-service et transaction-service.
 */
@Service
public class BankAgent {

    private static final Logger log = LoggerFactory.getLogger(BankAgent.class);

    private static final String SYSTEM = """
            Tu es l'assistant de la banque ADRIA. Tu réponds en français, de façon courte et précise.
            Tu n'inventes jamais un solde, un compte ou une transaction : utilise toujours les tools.
            Les comptes peuvent être désignés par leur UUID ou leur référence client (ex. CUST-001).
            Avant une opération d'écriture (paiement, dépôt, virement, blocage), rappelle le montant et les comptes,
            puis exécute-la si la demande est explicite. Si un tool renvoie une erreur métier (solde insuffisant,
            compte bloqué, devise différente, plafond dépassé), explique-la simplement.
            """;

    private final ChatClient chatClient;
    private final List<ToolCallback> tools;

    public BankAgent(ChatClient.Builder builder, ObjectProvider<ToolCallbackProvider> providers) {
        this.tools = providers.orderedStream()
                .flatMap(p -> Arrays.stream(p.getToolCallbacks()))
                .toList();
        log.info("Agent bancaire initialisé avec {} tools MCP", tools.size());
        this.chatClient = builder.defaultSystem(SYSTEM).defaultToolCallbacks(tools).build();
    }

    public String ask(String question) {
        return chatClient.prompt().user(question).call().content();
    }

    public List<String> toolNames() {
        return tools.stream().map(t -> t.getToolDefinition().name()).toList();
    }
}
