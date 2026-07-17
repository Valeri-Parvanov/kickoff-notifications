package bg.softuni.kickoffnotifications.service.impl;

import bg.softuni.kickoffnotifications.dto.BroadcastRequest;
import bg.softuni.kickoffnotifications.dto.NotificationDto;
import bg.softuni.kickoffnotifications.dto.NotifyRequest;
import bg.softuni.kickoffnotifications.model.Notification;
import bg.softuni.kickoffnotifications.model.enums.NotificationType;
import bg.softuni.kickoffnotifications.repository.NotificationRepository;
import bg.softuni.kickoffnotifications.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void broadcast_withSubscribers_createsNotificationPerUser() {
        UUID matchId = UUID.randomUUID();
        UUID homeTeamId = UUID.randomUUID();
        UUID awayTeamId = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        BroadcastRequest request = new BroadcastRequest();
        request.setMatchId(matchId);
        request.setHomeTeamId(homeTeamId);
        request.setAwayTeamId(awayTeamId);
        request.setMessage("Match ended 2-1");
        request.setType(NotificationType.MATCH_RESULT);

        when(subscriptionRepository.findDistinctUserIdsByEntityIdIn(anyList()))
                .thenReturn(List.of(user1, user2));
        when(notificationRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<UUID> notified = notificationService.broadcast(request);

        assertThat(notified).containsExactlyInAnyOrder(user1, user2);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue()).allMatch(n -> n.getMessage().equals("Match ended 2-1"));
    }

    @Test
    void broadcast_withNoSubscribers_returnsEmptyList() {
        BroadcastRequest request = new BroadcastRequest();
        request.setMatchId(UUID.randomUUID());
        request.setHomeTeamId(UUID.randomUUID());
        request.setAwayTeamId(UUID.randomUUID());
        request.setMessage("Match ended 0-0");

        when(subscriptionRepository.findDistinctUserIdsByEntityIdIn(anyList()))
                .thenReturn(List.of());

        List<UUID> notified = notificationService.broadcast(request);

        assertThat(notified).isEmpty();
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void countUnread_delegatesToRepository() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.countByUserIdAndReadFalse(userId)).thenReturn(5L);

        long count = notificationService.countUnread(userId);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void markAllRead_delegatesToRepositoryAndReturnsCount() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.markAllReadForUser(userId)).thenReturn(4);

        int updated = notificationService.markAllRead(userId);

        assertThat(updated).isEqualTo(4);
        verify(notificationRepository).markAllReadForUser(userId);
    }

    @Test
    void broadcast_looksUpSubscribersByMatchIdTeamsAndLeague() {
        UUID matchId = UUID.randomUUID();
        UUID homeTeamId = UUID.randomUUID();
        UUID awayTeamId = UUID.randomUUID();
        UUID leagueId = UUID.randomUUID();

        BroadcastRequest request = new BroadcastRequest();
        request.setMatchId(matchId);
        request.setHomeTeamId(homeTeamId);
        request.setAwayTeamId(awayTeamId);
        request.setLeagueId(leagueId);
        request.setMessage("Kick off");
        request.setType(NotificationType.MATCH_KICKOFF);

        when(subscriptionRepository.findDistinctUserIdsByEntityIdIn(anyList()))
                .thenReturn(List.of(UUID.randomUUID()));
        when(notificationRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        notificationService.broadcast(request);

        ArgumentCaptor<List<UUID>> captor = ArgumentCaptor.forClass(List.class);
        verify(subscriptionRepository).findDistinctUserIdsByEntityIdIn(captor.capture());
        assertThat(captor.getValue()).contains(matchId, homeTeamId, awayTeamId, leagueId);
    }

    @Test
    void notifyUser_savesSingleNotificationForThatUser() {
        UUID userId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();

        NotifyRequest request = new NotifyRequest();
        request.setUserId(userId);
        request.setMatchId(matchId);
        request.setMessage("LIVE: Home 1:0 Away (32')");
        request.setType(NotificationType.MATCH_UPDATE);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationDto dto = notificationService.notifyUser(request);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getMatchId()).isEqualTo(matchId);
        assertThat(saved.getType()).isEqualTo(NotificationType.MATCH_UPDATE);
        assertThat(dto.getMessage()).isEqualTo("LIVE: Home 1:0 Away (32')");
    }
}
