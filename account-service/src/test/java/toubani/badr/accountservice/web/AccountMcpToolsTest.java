package toubani.badr.accountservice.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import toubani.badr.accountservice.dto.CreateAccountRequest;
import toubani.badr.accountservice.repository.AccountRepository;
import toubani.badr.accountservice.service.AccountService;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AccountMcpToolsTest {

    @Autowired
    private ToolCallbackProvider accountTools;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void cleanUp() {
        accountRepository.deleteAll();
    }

    @Test
    void allToolsArePublished() {
        assertThat(Arrays.stream(accountTools.getToolCallbacks()).map(t -> t.getToolDefinition().name()))
                .containsExactlyInAnyOrder("listAccounts", "getAccount", "getBalance",
                        "createAccount", "blockAccount", "activateAccount");
    }

    @Test
    void getBalanceAcceptsCustomerReference() {
        accountService.create(new CreateAccountRequest("CUST-400", "EUR", 750));

        ToolCallback getBalance = Arrays.stream(accountTools.getToolCallbacks())
                .filter(t -> t.getToolDefinition().name().equals("getBalance"))
                .findFirst().orElseThrow();

        String result = getBalance.call("{\"account\":\"CUST-400\"}");
        assertThat(result).contains("750").contains("EUR");
    }
}
