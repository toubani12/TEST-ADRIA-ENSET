package toubani.badr.accountservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import toubani.badr.accountservice.dto.CreateAccountRequest;
import toubani.badr.accountservice.repository.AccountRepository;
import toubani.badr.accountservice.service.AccountService;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) {
        if (accountRepository.count() > 0) return;
        accountService.create(new CreateAccountRequest("CUST-001", "EUR", 5000));
        accountService.create(new CreateAccountRequest("CUST-002", "EUR", 1200));
        accountService.create(new CreateAccountRequest("CUST-003", "MAD", 20000));
        var blocked = accountService.create(new CreateAccountRequest("CUST-004", "EUR", 300));
        accountService.block(blocked.id());
        log.info("Données de démo : {} comptes créés", accountRepository.count());
    }
}
