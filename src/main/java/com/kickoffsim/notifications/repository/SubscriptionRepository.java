package com.kickoffsim.notifications.repository;

import com.kickoffsim.notifications.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUserId(UUID userId);

    boolean existsByUserIdAndEntityId(UUID userId, UUID entityId);

    @Query("SELECT DISTINCT s.userId FROM Subscription s WHERE s.entityId IN :entityIds")
    List<UUID> findDistinctUserIdsByEntityIdIn(@Param("entityIds") List<UUID> entityIds);
}
