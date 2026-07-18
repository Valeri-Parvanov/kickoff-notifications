package com.kickoffsim.notifications.service;

import com.kickoffsim.notifications.dto.SubscriptionDto;
import com.kickoffsim.notifications.dto.SubscriptionRequest;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {

    SubscriptionDto subscribe(SubscriptionRequest request);

    void unsubscribe(UUID id);

    List<SubscriptionDto> getForUser(UUID userId);

    boolean isSubscribed(UUID userId, UUID entityId);
}
