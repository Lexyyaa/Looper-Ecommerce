package com.loopers.domain.eventhandle;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EventHandledService {

    private final EventHandledRepository eventHandledRepository;

    @Transactional
    public boolean isExistEvent(String handler, String eventId) {
        if (eventHandledRepository.existsByEventIdAndHandler(eventId, handler)) {
            return false;
        } else {
            return true;
        }
    }


    public boolean save(String handler, String eventId) {
        EventHandled eventHandled = EventHandled.builder().
            eventId(eventId).
            handler(handler).
            handledAt(Instant.now())
            .build();

        eventHandledRepository.save(eventHandled);
        return true;
    }
}
