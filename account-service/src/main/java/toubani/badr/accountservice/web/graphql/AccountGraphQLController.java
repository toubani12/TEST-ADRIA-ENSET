package toubani.badr.accountservice.web.graphql;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import toubani.badr.accountservice.dto.AccountResponse;
import toubani.badr.accountservice.dto.CreateAccountRequest;
import toubani.badr.accountservice.service.AccountService;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountGraphQLController {

    private final AccountService accountService;

    @QueryMapping
    public List<AccountResponse> accounts() {
        return accountService.findAll();
    }

    @QueryMapping
    public AccountResponse accountById(@Argument UUID id) {
        return accountService.findById(id);
    }

    @QueryMapping
    public AccountResponse accountByReference(@Argument String reference) {
        return accountService.findByCustomerReference(reference);
    }

    @MutationMapping
    public AccountResponse createAccount(@Argument CreateAccountRequest input) {
        return accountService.create(input);
    }

    @MutationMapping
    public AccountResponse blockAccount(@Argument UUID id) {
        return accountService.block(id);
    }

    @MutationMapping
    public AccountResponse activateAccount(@Argument UUID id) {
        return accountService.activate(id);
    }
}
