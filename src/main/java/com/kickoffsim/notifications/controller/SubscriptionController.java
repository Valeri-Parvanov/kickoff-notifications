package com.kickoffsim.notifications.controller;

import com.kickoffsim.notifications.dto.SubscriptionDto;
import com.kickoffsim.notifications.dto.SubscriptionRequest;
import com.kickoffsim.notifications.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDto subscribe(@Valid @RequestBody SubscriptionRequest request) {
        return subscriptionService.subscribe(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable UUID id) {
        subscriptionService.unsubscribe(id);
    }

    @GetMapping
    public List<SubscriptionDto> getForUser(@RequestParam UUID userId) {
        return subscriptionService.getForUser(userId);
    }

    @GetMapping("/check")
    public boolean isSubscribed(@RequestParam UUID userId, @RequestParam UUID entityId) {
        return subscriptionService.isSubscribed(userId, entityId);
    }
}
