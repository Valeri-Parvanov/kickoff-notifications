package bg.softuni.kickoffnotifications.service;

import bg.softuni.kickoffnotifications.dto.SubscriptionDto;
import bg.softuni.kickoffnotifications.dto.SubscriptionRequest;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {

    SubscriptionDto subscribe(SubscriptionRequest request);

    void unsubscribe(UUID id);

    List<SubscriptionDto> getForUser(UUID userId);

    boolean isSubscribed(UUID userId, UUID entityId);
}
