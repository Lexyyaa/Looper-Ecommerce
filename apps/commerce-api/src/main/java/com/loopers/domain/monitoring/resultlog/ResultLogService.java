package com.loopers.domain.monitoring.resultlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.shared.event.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResultLogService {

    private final ObjectMapper om = new ObjectMapper();

    public void write(Envelope<? extends ResultLogPayload> env)  {
        try {
            var n = om.createObjectNode();
            n.put("id", env.id());
            n.put("at", env.at().toString());
            n.put("actorId", env.actorId());
            n.put("kind", env.payload().getClass().getSimpleName());
            n.set("payload", om.valueToTree(env.payload()));
            log.info("resultlog {}", n);

            Thread.sleep(300);
        } catch (Exception ex) {
            log.warn("resultlog failed", ex);
        }
    }
}
