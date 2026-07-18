package com.kickoffsim.notifications.controller;

import com.kickoffsim.notifications.dto.BroadcastRequest;
import com.kickoffsim.notifications.dto.NotificationDto;
import com.kickoffsim.notifications.dto.NotifyRequest;
import com.kickoffsim.notifications.model.enums.NotificationType;
import com.kickoffsim.notifications.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void broadcast_validRequest_returnsNotifiedUserIds() throws Exception {
        UUID user1 = UUID.randomUUID();
        BroadcastRequest request = new BroadcastRequest();
        request.setMatchId(UUID.randomUUID());
        request.setHomeTeamId(UUID.randomUUID());
        request.setAwayTeamId(UUID.randomUUID());
        request.setMessage("Match ended 2-1");

        when(notificationService.broadcast(any())).thenReturn(List.of(user1));

        mockMvc.perform(post("/api/notifications/broadcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0]").value(user1.toString()));
    }

    @Test
    void broadcast_missingMessage_returns400() throws Exception {
        BroadcastRequest request = new BroadcastRequest();
        request.setMatchId(UUID.randomUUID());
        request.setHomeTeamId(UUID.randomUUID());
        request.setAwayTeamId(UUID.randomUUID());

        mockMvc.perform(post("/api/notifications/broadcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notifyUser_validRequest_returnsCreatedNotification() throws Exception {
        UUID userId = UUID.randomUUID();
        NotifyRequest request = new NotifyRequest();
        request.setUserId(userId);
        request.setMatchId(UUID.randomUUID());
        request.setMessage("LIVE: Home 1:0 Away");
        request.setType(NotificationType.MATCH_UPDATE);

        NotificationDto dto = new NotificationDto();
        dto.setId(UUID.randomUUID());
        dto.setUserId(userId);
        dto.setMessage("LIVE: Home 1:0 Away");
        dto.setType(NotificationType.MATCH_UPDATE);
        dto.setCreatedAt(LocalDateTime.now());

        when(notificationService.notifyUser(any())).thenReturn(dto);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("LIVE: Home 1:0 Away"));
    }

    @Test
    void notifyUser_missingUserId_returns400() throws Exception {
        NotifyRequest request = new NotifyRequest();
        request.setMessage("LIVE update");

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void markAllRead_returnsNoContentAndDelegates() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(put("/api/notifications/read-all")
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());

        verify(notificationService).markAllRead(userId);
    }

    @Test
    void unreadCount_returnsCount() throws Exception {
        UUID userId = UUID.randomUUID();
        when(notificationService.countUnread(userId)).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void getForUser_returnsNotificationList() throws Exception {
        UUID userId = UUID.randomUUID();
        NotificationDto dto = new NotificationDto();
        dto.setId(UUID.randomUUID());
        dto.setUserId(userId);
        dto.setMessage("Test notification");
        dto.setType(NotificationType.MATCH_RESULT);
        dto.setRead(false);
        dto.setCreatedAt(LocalDateTime.now());

        when(notificationService.getForUser(userId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notifications")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Test notification"));
    }

    @Test
    void markRead_returnsNoContentAndDelegates() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/notifications/{id}/read", id))
                .andExpect(status().isNoContent());

        verify(notificationService).markRead(id);
    }

    @Test
    void clearAll_returnsNoContentAndDelegates() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/notifications")
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());

        verify(notificationService).clearAll(userId);
    }
}
