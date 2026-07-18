package com.kickoffsim.notifications.service.impl;

import com.kickoffsim.notifications.dto.SubscriptionDto;
import com.kickoffsim.notifications.dto.SubscriptionRequest;
import com.kickoffsim.notifications.exception.ResourceNotFoundException;
import com.kickoffsim.notifications.model.Subscription;
import com.kickoffsim.notifications.model.enums.EntityType;
import com.kickoffsim.notifications.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    void subscribe_savesAndReturnsDto() {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        SubscriptionRequest request = new SubscriptionRequest();
        request.setUserId(userId);
        request.setEntityType(EntityType.TEAM);
        request.setEntityId(entityId);

        Subscription saved = new Subscription();
        saved.setId(UUID.randomUUID());
        saved.setUserId(userId);
        saved.setEntityType(EntityType.TEAM);
        saved.setEntityId(entityId);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(saved);

        SubscriptionDto dto = subscriptionService.subscribe(request);

        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getEntityType()).isEqualTo(EntityType.TEAM);
        assertThat(dto.getEntityId()).isEqualTo(entityId);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void unsubscribe_existing_deletes() {
        UUID id = UUID.randomUUID();
        when(subscriptionRepository.existsById(id)).thenReturn(true);

        subscriptionService.unsubscribe(id);

        verify(subscriptionRepository).deleteById(id);
    }

    @Test
    void unsubscribe_missing_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(subscriptionRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> subscriptionService.unsubscribe(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(subscriptionRepository, never()).deleteById(any());
    }

    @Test
    void getForUser_mapsToDtos() {
        UUID userId = UUID.randomUUID();
        Subscription sub = new Subscription();
        sub.setId(UUID.randomUUID());
        sub.setUserId(userId);
        sub.setEntityType(EntityType.LEAGUE);
        sub.setEntityId(UUID.randomUUID());
        when(subscriptionRepository.findByUserId(userId)).thenReturn(List.of(sub));

        List<SubscriptionDto> result = subscriptionService.getForUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityType()).isEqualTo(EntityType.LEAGUE);
    }

    @Test
    void isSubscribed_delegatesToRepository() {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        when(subscriptionRepository.existsByUserIdAndEntityId(userId, entityId)).thenReturn(true);

        assertThat(subscriptionService.isSubscribed(userId, entityId)).isTrue();
    }
}
