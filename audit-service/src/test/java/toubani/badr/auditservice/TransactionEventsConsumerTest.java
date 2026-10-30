package toubani.badr.auditservice;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import toubani.badr.auditservice.consumer.TransactionEventsConsumer;
import toubani.badr.auditservice.domain.AuditEntry;
import toubani.badr.auditservice.repository.AuditEntryRepository;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransactionEventsConsumerTest {

    @Test
    void journaliseLEvenementRecuAvecSaCle() {
        AuditEntryRepository repository = mock(AuditEntryRepository.class);
        TransactionEventsConsumer consumer = new TransactionEventsConsumer(repository);
        String json = "{\"eventType\":\"TransactionCreated\",\"amount\":250.00}";

        consumer.onEvent(new ConsumerRecord<>("bank.transactions.created", 1, 42L, "acc-1", json));

        verify(repository).save(argThat((AuditEntry e) -> e.getMessageKey().equals("acc-1")
                && e.getOffsetNo() == 42L && e.getPayload().equals(json)));
    }
}
