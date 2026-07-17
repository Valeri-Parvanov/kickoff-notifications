package bg.softuni.kickoffnotifications.controller;

import bg.softuni.kickoffnotifications.dto.SubscriptionDto;
import bg.softuni.kickoffnotifications.dto.SubscriptionRequest;
import bg.softuni.kickoffnotifications.exception.ResourceNotFoundException;
import bg.softuni.kickoffnotifications.model.enums.EntityType;
import bg.softuni.kickoffnotifications.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void subscribe_validRequest_returnsCreated() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        SubscriptionRequest request = new SubscriptionRequest();
        request.setUserId(userId);
        request.setEntityType(EntityType.TEAM);
        request.setEntityId(entityId);

        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(UUID.randomUUID());
        dto.setUserId(userId);
        dto.setEntityType(EntityType.TEAM);
        dto.setEntityId(entityId);
        when(subscriptionService.subscribe(any())).thenReturn(dto);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entityType").value("TEAM"));
    }

    @Test
    void subscribe_duplicate_returns409() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest();
        request.setUserId(UUID.randomUUID());
        request.setEntityType(EntityType.TEAM);
        request.setEntityId(UUID.randomUUID());
        when(subscriptionService.subscribe(any()))
                .thenThrow(new DataIntegrityViolationException("dup"));

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void subscribe_missingFields_returns400() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest();

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unsubscribe_returnsNoContentAndDelegates() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/subscriptions/{id}", id))
                .andExpect(status().isNoContent());

        verify(subscriptionService).unsubscribe(id);
    }

    @Test
    void unsubscribe_missing_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Subscription not found: " + id))
                .when(subscriptionService).unsubscribe(id);

        mockMvc.perform(delete("/api/subscriptions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getForUser_returnsList() throws Exception {
        UUID userId = UUID.randomUUID();
        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(UUID.randomUUID());
        dto.setUserId(userId);
        dto.setEntityType(EntityType.LEAGUE);
        dto.setEntityId(UUID.randomUUID());
        when(subscriptionService.getForUser(userId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/subscriptions").param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entityType").value("LEAGUE"));
    }

    @Test
    void isSubscribed_returnsBoolean() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        when(subscriptionService.isSubscribed(userId, entityId)).thenReturn(true);

        mockMvc.perform(get("/api/subscriptions/check")
                        .param("userId", userId.toString())
                        .param("entityId", entityId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
