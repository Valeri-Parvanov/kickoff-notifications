package bg.softuni.kickoffnotifications.controller;

import bg.softuni.kickoffnotifications.dto.BroadcastRequest;
import bg.softuni.kickoffnotifications.dto.NotificationDto;
import bg.softuni.kickoffnotifications.dto.NotifyRequest;
import bg.softuni.kickoffnotifications.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/broadcast")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> broadcast(@Valid @RequestBody BroadcastRequest request) {
        return notificationService.broadcast(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationDto notifyUser(@Valid @RequestBody NotifyRequest request) {
        return notificationService.notifyUser(request);
    }

    @GetMapping
    public List<NotificationDto> getForUser(@RequestParam UUID userId) {
        return notificationService.getForUser(userId);
    }

    @GetMapping("/unread-count")
    public long unreadCount(@RequestParam UUID userId) {
        return notificationService.countUnread(userId);
    }

    @PutMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable UUID id) {
        notificationService.markRead(id);
    }

    @PutMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@RequestParam UUID userId) {
        notificationService.markAllRead(userId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearAll(@RequestParam UUID userId) {
        notificationService.clearAll(userId);
    }
}
