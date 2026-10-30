package toubani.badr.accountservice.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import toubani.badr.accountservice.dto.CreateAccountRequest;
import toubani.badr.accountservice.repository.AccountRepository;
import toubani.badr.accountservice.service.AccountService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@ActiveProfiles("test")
class AccountGraphQLTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void cleanUp() {
        accountRepository.deleteAll();
    }

    @Test
    void queryAccounts() {
        accountService.create(new CreateAccountRequest("CUST-300", "EUR", 10));

        graphQlTester.document("{ accounts { customerReference status balance currency } }")
                .execute()
                .path("accounts[0].customerReference").entity(String.class).isEqualTo("CUST-300")
                .path("accounts[0].status").entity(String.class).isEqualTo("ACTIVE");
    }

    @Test
    void mutationCreateAccount() {
        graphQlTester.document("""
                        mutation {
                          createAccount(input: {customerReference: "CUST-301", currency: "MAD", initialBalance: 500}) {
                            id status balance
                          }
                        }""")
                .execute()
                .path("createAccount.status").entity(String.class).isEqualTo("ACTIVE")
                .path("createAccount.balance").entity(Double.class).isEqualTo(500.0);

        assertThat(accountRepository.findByCustomerReference("CUST-301")).isPresent();
    }

    @Test
    void unknownReferenceReturnsNotFoundError() {
        graphQlTester.document("{ accountByReference(reference: \"CUST-999\") { id } }")
                .execute()
                .errors()
                .expect(e -> e.getMessage().contains("CUST-999"));
    }
}
