package com.eduapp.backend.controller;

import jakarta.validation.Valid;

import com.eduapp.backend.model.Notification;
import com.eduapp.backend.security.AuthenticatedUser;
import com.eduapp.backend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // N-SC2-A: returns ONLY the authenticated caller's notifications (was all users').
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        List<Notification> notifications = notificationService.findByUserId(principal.getUserId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getById(@PathVariable Long id) {
        logger.info("Fetching notification with ID: {}", id);
        Optional<Notification> notification = notificationService.findById(id);
        return notification.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Notification> create(@Valid @RequestBody Notification notification) {
        logger.info("Creating notification");
        Notification saved = notificationService.save(notification);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notification> update(@PathVariable Long id, @Valid @RequestBody Notification notification) {
        logger.info("Updating notification with ID: {}", id);
        if (!notificationService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        notification.setId(id);
        Notification updated = notificationService.save(notification);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting notification with ID: {}", id);
        if (!notificationService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        notificationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}