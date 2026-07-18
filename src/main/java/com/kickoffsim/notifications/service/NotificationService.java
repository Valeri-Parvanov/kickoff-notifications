package com.kickoffsim.notifications.service;

import com.kickoffsim.notifications.dto.BroadcastRequest;
import com.kickoffsim.notifications.dto.NotificationDto;
import com.kickoffsim.notifications.dto.NotifyRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<UUID> broadcast(BroadcastRequest request);

    NotificationDto notifyUser(NotifyRequest request);

    List<NotificationDto> getForUser(UUID userId);

    long countUnread(UUID userId);

    void markRead(UUID id);

    int markAllRead(UUID userId);

    void clearAll(UUID userId);

    void deleteOlderThan(LocalDateTime cutoff);
}
