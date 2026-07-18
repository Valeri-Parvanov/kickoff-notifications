package com.kickoffsim.notifications.service.impl;

import com.kickoffsim.notifications.dto.NotificationDto;
import com.kickoffsim.notifications.exception.ResourceNotFoundException;
import com.kickoffsim.notifications.model.Notification;
import com.kickoffsim.notifications.model.enums.NotificationType;
import com.kickoffsim.notifications.repository.NotificationRepository;
import com.kickoffsim.notifications.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplCoverageTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private SubscriptionRepository subscriptionRepository;

    @InjectMocks private NotificationServiceImpl service;

    private Notification notification(UUID id) {
        Notification n = new Notification();
        n.setId(id);
        n.setUserId(UUID.randomUUID());
        n.setMessage("msg");
        n.setType(NotificationType.GOAL);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }

    @Test
    void markRead_existing_savesAsRead() {
        UUID id = UUID.randomUUID();
        Notification n = notification(id);
        when(notificationRepository.findById(id)).thenReturn(Optional.of(n));

        service.markRead(id);

        assertThat(n.isRead()).isTrue();
        verify(notificationRepository).save(n);
    }

    @Test
    void markRead_missing_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void clearAll_delegatesToRepository() {
        UUID userId = UUID.randomUUID();

        service.clearAll(userId);

        verify(notificationRepository).deleteAllByUserId(userId);
    }

    @Test
    void deleteOlderThan_delegatesToRepository() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        service.deleteOlderThan(cutoff);

        verify(notificationRepository).deleteOlderThan(cutoff);
    }

    @Test
    void getForUser_mapsToDtos() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(notification(UUID.randomUUID())));

        List<NotificationDto> result = service.getForUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo("msg");
    }
}
