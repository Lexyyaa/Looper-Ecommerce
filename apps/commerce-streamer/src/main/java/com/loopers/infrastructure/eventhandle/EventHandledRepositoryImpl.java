package com.loopers.infrastructure.eventhandle;

import com.loopers.domain.eventhandle.EventHandled;
import com.loopers.domain.eventhandle.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventHandledRepositoryImpl implements EventHandledRepository {

    private final EventHandledJpaRepository eventHandledJpaRepository;

    @Override
    public EventHandled save(EventHandled eventHandled) {
        return eventHandledJpaRepository.save(eventHandled);
    }

    @Override
    public boolean existsByEventIdAndHandler(String eventId, String handler) {
        return eventHandledJpaRepository.existsByEventIdAndHandler(eventId,handler);
    }

    @Override
    public List<EventHandled> saveAll(List<EventHandled> events) {
        return eventHandledJpaRepository.saveAll(events);
    }
}
