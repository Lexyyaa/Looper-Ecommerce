package com.loopers.domain.eventhandle;

import java.util.List;

public interface EventHandledRepository {
    EventHandled save(EventHandled eventHandled);
    boolean existsByEventIdAndHandler(String eventId, String handler);
    List<EventHandled> saveAll(List<EventHandled> events);
}
