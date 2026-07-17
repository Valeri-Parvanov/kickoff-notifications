package bg.softuni.kickoffnotifications.dto;

import bg.softuni.kickoffnotifications.model.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SubscriptionDto {

    private UUID id;
    private UUID userId;
    private EntityType entityType;
    private UUID entityId;
    private LocalDateTime createdAt;
}
