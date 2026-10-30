package toubani.badr.transactionservice.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import toubani.badr.transactionservice.mcp.TransactionMcpTools;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider transactionTools(TransactionMcpTools transactionMcpTools) {
        return MethodToolCallbackProvider.builder().toolObjects(transactionMcpTools).build();
    }
}
