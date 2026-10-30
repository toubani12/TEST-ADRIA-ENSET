package toubani.badr.auditservice.domain;

import jakarta.persistence.*;

import java.time.Instant;

/** Trace d'un événement reçu : le service d'audit ne modifie jamais les données bancaires. */
@Entity
@Table(name = "audit_entries")
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String messageKey;
    private int partitionNo;
    private long offsetNo;
    @Column(length = 2000)
    private String payload;
    private Instant receivedAt;

    protected AuditEntry() {
    }

    public AuditEntry(String messageKey, int partitionNo, long offsetNo, String payload) {
        this.messageKey = messageKey;
        this.partitionNo = partitionNo;
        this.offsetNo = offsetNo;
        this.payload = payload;
        this.receivedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getMessageKey() { return messageKey; }
    public int getPartitionNo() { return partitionNo; }
    public long getOffsetNo() { return offsetNo; }
    public String getPayload() { return payload; }
    public Instant getReceivedAt() { return receivedAt; }
}
