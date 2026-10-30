package toubani.badr.transactionservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toubani.badr.transactionservice.domain.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    List<Transaction> findAllByOrderByCreatedAtDesc();
}
