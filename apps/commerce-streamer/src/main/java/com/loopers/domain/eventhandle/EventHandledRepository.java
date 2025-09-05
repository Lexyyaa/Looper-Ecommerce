package com.loopers.domain.eventhandle;

public interface EventHandledRepository {
    EventHandled save(EventHandled eventHandled);
    boolean existsByEventIdAndHandler(String eventId, String handler);
}
