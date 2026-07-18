package com.kickoffsim.notifications.repository;

import com.kickoffsim.notifications.model.Subscription;
import com.kickoffsim.notifications.model.enums.EntityType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    void findByUserId_returnsOnlyUserSubscriptions() {
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        Subscription s1 = subscription(userId, EntityType.TEAM, teamId);
        Subscription s2 = subscription(otherId, EntityType.TEAM, teamId);
        subscriptionRepository.saveAll(List.of(s1, s2));

        List<Subscription> result = subscriptionRepository.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void findDistinctUserIdsByEntityIdIn_returnsSubscribedUsers() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        subscriptionRepository.saveAll(List.of(
                subscription(user1, EntityType.TEAM, teamId),
                subscription(user2, EntityType.TEAM, teamId)
        ));

        List<UUID> userIds = subscriptionRepository.findDistinctUserIdsByEntityIdIn(List.of(teamId));

        assertThat(userIds).containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    void uniqueConstraint_preventsduplicateSubscription() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        subscriptionRepository.save(subscription(userId, EntityType.TEAM, teamId));
        subscriptionRepository.flush();

        assertThatThrownBy(() -> {
            subscriptionRepository.save(subscription(userId, EntityType.TEAM, teamId));
            subscriptionRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Subscription subscription(UUID userId, EntityType type, UUID entityId) {
        Subscription s = new Subscription();
        s.setUserId(userId);
        s.setEntityType(type);
        s.setEntityId(entityId);
        return s;
    }
}
