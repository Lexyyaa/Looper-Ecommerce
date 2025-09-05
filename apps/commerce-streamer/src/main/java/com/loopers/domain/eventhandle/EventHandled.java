package com.loopers.domain.eventhandle;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="event_handled",
        uniqueConstraints=@UniqueConstraint(name="uk_event_handler", columnNames={"eventId","handler"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventHandled {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    private String handler;

    private Instant handledAt;
}
