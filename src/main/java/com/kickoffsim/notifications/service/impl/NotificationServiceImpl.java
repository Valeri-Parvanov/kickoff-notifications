package com.kickoffsim.notifications.service.impl;

import com.kickoffsim.notifications.dto.BroadcastRequest;
import com.kickoffsim.notifications.dto.NotificationDto;
import com.kickoffsim.notifications.dto.NotifyRequest;
import com.kickoffsim.notifications.exception.ResourceNotFoundException;
import com.kickoffsim.notifications.model.enums.NotificationType;
import com.kickoffsim.notifications.model.Notification;
import com.kickoffsim.notifications.repository.NotificationRepository;
import com.kickoffsim.notifications.repository.SubscriptionRepository;
import com.kickoffsim.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public List<UUID> broadcast(BroadcastRequest request) {
        List<UUID> entityIds = Stream.of(request.getMatchId(), request.getHomeTeamId(), request.getAwayTeamId(), request.getLeagueId())
                .filter(Objects::nonNull)
                .toList();

        List<UUID> userIds = subscriptionRepository.findDistinctUserIdsByEntityIdIn(entityIds);

        if (userIds.isEmpty()) {
            log.info("No subscribers for match {}, skipping broadcast", request.getMatchId());
            return List.of();
        }

        List<Notification> notifications = userIds.stream()
                .map(userId -> {
                    Notification n = new Notification();
                    n.setUserId(userId);
                    n.setMessage(request.getMessage());
                    n.setMatchId(request.getMatchId());
                    n.setType(request.getType() != null ? request.getType() : com.kickoffsim.notifications.model.enums.NotificationType.MATCH_RESULT);
                    return n;
                })
                .toList();

        notificationRepository.saveAll(notifications);
        log.info("Broadcasted {} notification(s) for match {}", notifications.size(), request.getMatchId());
        return userIds;
    }

    @Override
    @Transactional
    public NotificationDto notifyUser(NotifyRequest request) {
        Notification n = new Notification();
        n.setUserId(request.getUserId());
        n.setMessage(request.getMessage());
        n.setMatchId(request.getMatchId());
        n.setType(request.getType() != null ? request.getType() : NotificationType.MATCH_RESULT);
        Notification saved = notificationRepository.save(n);
        log.info("Sent direct notification to user {} for match {}", request.getUserId(), request.getMatchId());
        return toDto(saved);
    }

    @Override
    public List<NotificationDto> getForUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public long countUnread(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public int markAllRead(UUID userId) {
        int updated = notificationRepository.markAllReadForUser(userId);
        log.info("Marked {} notification(s) as read for user {}", updated, userId);
        return updated;
    }

    @Override
    @Transactional
    public void clearAll(UUID userId) {
        notificationRepository.deleteAllByUserId(userId);
        log.info("Cleared all notifications for user {}", userId);
    }

    @Override
    @Transactional
    public void deleteOlderThan(LocalDateTime cutoff) {
        notificationRepository.deleteOlderThan(cutoff);
        log.info("Deleted notifications older than {}", cutoff);
    }

    private NotificationDto toDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setUserId(n.getUserId());
        dto.setMessage(n.getMessage());
        dto.setMatchId(n.getMatchId());
        dto.setType(n.getType());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
