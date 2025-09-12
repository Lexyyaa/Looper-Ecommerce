package com.loopers.domain.eventlog;

import java.util.List;

public interface EventLogRepository {
    EventLog save(EventLog eventLog);
    List<EventLog> saveAll(List<EventLog> logs);
}
