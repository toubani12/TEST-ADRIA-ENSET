package toubani.badr.accountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toubani.badr.accountservice.domain.Account;
import toubani.badr.accountservice.domain.AccountStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByCustomerReference(String customerReference);

    boolean existsByCustomerReference(String customerReference);

    List<Account> findByStatus(AccountStatus status);
}
