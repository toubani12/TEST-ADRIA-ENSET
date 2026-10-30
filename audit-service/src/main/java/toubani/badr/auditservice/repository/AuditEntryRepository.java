package toubani.badr.auditservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toubani.badr.auditservice.domain.AuditEntry;

import java.util.List;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {
    List<AuditEntry> findTop100ByOrderByIdDesc();
}
