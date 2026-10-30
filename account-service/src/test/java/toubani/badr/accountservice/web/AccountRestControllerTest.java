package toubani.badr.accountservice.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import toubani.badr.accountservice.dto.CreateAccountRequest;
import toubani.badr.accountservice.repository.AccountRepository;
import toubani.badr.accountservice.service.AccountService;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void cleanUp() {
        accountRepository.deleteAll();
    }

    @Test
    void createReturns201() throws Exception {
        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerReference\":\"CUST-200\",\"currency\":\"EUR\",\"initialBalance\":100}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void invalidCurrencyReturns400() throws Exception {
        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerReference\":\"CUST-201\",\"currency\":\"euro\",\"initialBalance\":100}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownAccountReturns404() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Compte introuvable"));
    }

    @Test
    void debitWithInsufficientBalanceReturns409() throws Exception {
        UUID id = accountService.create(new CreateAccountRequest("CUST-202", "EUR", 50)).id();

        mockMvc.perform(post("/api/accounts/{id}/debit", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100}"))
                .andExpect(status().isConflict());
    }

    @Test
    void balanceEndpointReturnsCurrencyAndStatus() throws Exception {
        UUID id = accountService.create(new CreateAccountRequest("CUST-203", "MAD", 1000)).id();

        mockMvc.perform(get("/api/accounts/{id}/balance", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.currency").value("MAD"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
