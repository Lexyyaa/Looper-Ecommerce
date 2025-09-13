package com.loopers.domain.eventhandle;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @Transactional(readOnly = true)
    public boolean shouldProcess(String handler, String eventId) {
        return eventId != null && !eventId.isBlank() && !isExistEvent(handler, eventId);
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

    @Transactional
    public void saveAll(String handler, List<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty())
            return;

        Instant now = Instant.now();
        List<EventHandled> batch = new ArrayList<>(eventIds.size());

        for (String id : eventIds) {
            if (id == null || id.isBlank())
                continue;
            batch.add(EventHandled.builder()
                    .eventId(id)
                    .handler(handler)
                    .handledAt(now)
                    .build());
        }
        if (!batch.isEmpty()) {
            eventHandledRepository.saveAll(batch);
        }
    }

}
