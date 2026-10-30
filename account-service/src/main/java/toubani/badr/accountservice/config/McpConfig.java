package toubani.badr.accountservice.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import toubani.badr.accountservice.mcp.AccountMcpTools;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider accountTools(AccountMcpTools accountMcpTools) {
        return MethodToolCallbackProvider.builder().toolObjects(accountMcpTools).build();
    }
}
