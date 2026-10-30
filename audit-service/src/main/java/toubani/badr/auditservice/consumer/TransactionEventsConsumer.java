package toubani.badr.auditservice.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import toubani.badr.auditservice.domain.AuditEntry;
import toubani.badr.auditservice.repository.AuditEntryRepository;

@Component
public class TransactionEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventsConsumer.class);
    private final AuditEntryRepository repository;

    public TransactionEventsConsumer(AuditEntryRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "${bank.kafka.topic:bank.transactions.created}", groupId = "audit-service")
    public void onEvent(ConsumerRecord<String, String> record) {
        log.info("[AUDIT] compte={} partition={} offset={} événement={}", record.key(), record.partition(),
                record.offset(), record.value());
        repository.save(new AuditEntry(record.key(), record.partition(), record.offset(), record.value()));
    }
}
