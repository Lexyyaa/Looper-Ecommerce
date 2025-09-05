package com.loopers.infrastructure.eventlog;

import com.loopers.domain.eventlog.EventLog;
import com.loopers.domain.eventlog.EventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventLogRepositoryImpl implements EventLogRepository {

    private final EventLogJpaRepository eventLogJpaRepository;

    @Override
    public EventLog save(EventLog eventLog) {
        return eventLogJpaRepository.save(eventLog);
    }
}
