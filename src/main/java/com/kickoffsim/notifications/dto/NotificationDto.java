package com.kickoffsim.notifications.dto;

import com.kickoffsim.notifications.model.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class NotificationDto {

    private UUID id;
    private UUID userId;
    private String message;
    private UUID matchId;
    private NotificationType type;
    private boolean read;
    private LocalDateTime createdAt;
}
