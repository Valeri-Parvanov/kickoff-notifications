package bg.softuni.kickoffnotifications.dto;

import bg.softuni.kickoffnotifications.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NotifyRequest {

    @NotNull
    private UUID userId;

    private UUID matchId;

    @NotBlank
    private String message;

    private NotificationType type = NotificationType.MATCH_RESULT;
}
