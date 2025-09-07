package com.loopers.domain.eventhandle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventHandledServiceTest {

    @Mock
    private EventHandledRepository eventHandledRepository;

    @InjectMocks
    private EventHandledService eventHandledService;

    @Nested
    @DisplayName("멱등 검증")
    class IsExistEvent {

        @Test
        @DisplayName("[성공] 처리 이력이 없으면 true 반환")
        void success_returnsTrueAndSavesWhenNotExists() {
            // Arrange
            String handler = "activity-metrics";
            String eventId = "evt-123";
            when(eventHandledRepository.existsByEventIdAndHandler(eventId, handler)).thenReturn(false);

            // Act
            boolean ok = eventHandledService.isExistEvent(handler, eventId);

            // Assert
            assertThat(ok).isTrue();
            verify(eventHandledRepository).existsByEventIdAndHandler(eq(eventId), eq(handler));
        }

        @Test
        @DisplayName("[성공] 처리 이력이 있으면 false 반환하고 이력을 저장하지않는다.")
        void success_returnsFalseAndNotSaveWhenExists() {
            // Arrange
            String handler = "activity-metrics";
            String eventId = "evt-999";
            when(eventHandledRepository.existsByEventIdAndHandler(eventId, handler))
                    .thenReturn(true);

            // Act
            boolean ok = eventHandledService.isExistEvent(handler, eventId);

            // Assert
            assertThat(ok).isFalse();
        }
    }
}
