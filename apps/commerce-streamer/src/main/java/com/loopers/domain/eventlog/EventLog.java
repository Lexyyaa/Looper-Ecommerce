package com.loopers.domain.eventlog;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="event_log", indexes=@Index(name="idx_type_at", columnList="eventType, occurredAt"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLog {
    @Id
    private String eventId;

    private String eventType;

    private String eventVersion;

    private Instant occurredAt;

    private String actorId;

    @Lob
    @Column(columnDefinition="LONGTEXT")
    private String payloadJson;

    @Lob
    @Column(columnDefinition="LONGTEXT")
    private String headersJson;

    private Instant receivedAt;
}
