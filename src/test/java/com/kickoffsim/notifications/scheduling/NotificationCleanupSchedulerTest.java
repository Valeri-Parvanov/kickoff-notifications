package com.kickoffsim.notifications.scheduling;

import com.kickoffsim.notifications.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationCleanupSchedulerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationCleanupScheduler scheduler;

    @Test
    void cleanupOldNotifications_deletesEntriesOlderThan30Days() {
        LocalDateTime before = LocalDateTime.now().minusDays(30);

        scheduler.cleanupOldNotifications();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(notificationService).deleteOlderThan(captor.capture());

        LocalDateTime after = LocalDateTime.now().minusDays(30);
        assertThat(captor.getValue())
                .isBetween(before.minusSeconds(5), after.plusSeconds(5));
    }
}
