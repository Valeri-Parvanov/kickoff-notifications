package com.kickoffsim.notifications.dto;

import com.kickoffsim.notifications.model.enums.EntityType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SubscriptionRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private EntityType entityType;

    @NotNull
    private UUID entityId;
}
