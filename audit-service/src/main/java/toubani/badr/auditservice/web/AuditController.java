package toubani.badr.auditservice.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import toubani.badr.auditservice.domain.AuditEntry;
import toubani.badr.auditservice.repository.AuditEntryRepository;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditEntryRepository repository;

    public AuditController(AuditEntryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AuditEntry> latest() {
        return repository.findTop100ByOrderByIdDesc();
    }
}
