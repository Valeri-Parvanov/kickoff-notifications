package com.kickoffsim.notifications.dto;

import com.kickoffsim.notifications.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BroadcastRequest {

    @NotNull
    private UUID matchId;

    @NotNull
    private UUID homeTeamId;

    @NotNull
    private UUID awayTeamId;

    private UUID leagueId;

    @NotBlank
    private String message;

    private NotificationType type = NotificationType.MATCH_RESULT;
}
