package com.loopers.infrastructure.eventhandle;

import com.loopers.domain.eventhandle.EventHandled;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventHandledJpaRepository extends JpaRepository<EventHandled, Long> {
    boolean existsByEventIdAndHandler(String eventId, String handler);
}

