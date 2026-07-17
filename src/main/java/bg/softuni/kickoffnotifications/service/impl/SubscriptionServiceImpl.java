package bg.softuni.kickoffnotifications.service.impl;

import bg.softuni.kickoffnotifications.dto.SubscriptionDto;
import bg.softuni.kickoffnotifications.dto.SubscriptionRequest;
import bg.softuni.kickoffnotifications.exception.ResourceNotFoundException;
import bg.softuni.kickoffnotifications.model.Subscription;
import bg.softuni.kickoffnotifications.repository.SubscriptionRepository;
import bg.softuni.kickoffnotifications.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public SubscriptionDto subscribe(SubscriptionRequest request) {
        Subscription subscription = new Subscription();
        subscription.setUserId(request.getUserId());
        subscription.setEntityType(request.getEntityType());
        subscription.setEntityId(request.getEntityId());
        Subscription saved = subscriptionRepository.save(subscription);
        log.info("User {} subscribed to {} {}", request.getUserId(), request.getEntityType(), request.getEntityId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public void unsubscribe(UUID id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subscription not found: " + id);
        }
        subscriptionRepository.deleteById(id);
        log.info("Subscription {} removed", id);
    }

    @Override
    public List<SubscriptionDto> getForUser(UUID userId) {
        return subscriptionRepository.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public boolean isSubscribed(UUID userId, UUID entityId) {
        return subscriptionRepository.existsByUserIdAndEntityId(userId, entityId);
    }

    private SubscriptionDto toDto(Subscription s) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(s.getId());
        dto.setUserId(s.getUserId());
        dto.setEntityType(s.getEntityType());
        dto.setEntityId(s.getEntityId());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
