package toubani.badr.transactionservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publie l'événement Kafka uniquement après le commit de la transaction JPA :
 * si la persistance échoue (rollback), rien n'est publié.
 */
@Component
@Slf4j
public class TransactionEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public TransactionEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                     @Value("${bank.kafka.topic:bank.transactions.created}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCreated(TransactionCreatedEvent event) {
        try {
            // clé = accountId : tous les événements d'un même compte vont dans la même partition (ordre garanti)
            kafkaTemplate.send(topic, event.accountId().toString(), event.toJson())
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.warn("Publication Kafka échouée pour la transaction {} : {}", event.transactionId(),
                                    error.getMessage());
                        } else {
                            log.info("Événement {} publié dans {} (partition {})", event.eventId(), topic,
                                    result.getRecordMetadata().partition());
                        }
                    });
        } catch (RuntimeException e) {
            log.warn("Kafka indisponible, événement {} non publié : {}", event.eventId(), e.getMessage());
        }
    }
}
