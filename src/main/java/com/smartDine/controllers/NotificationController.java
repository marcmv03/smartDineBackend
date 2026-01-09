package com.smartDine.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.NotificationDTO;
import com.smartDine.entity.Notification;
import com.smartDine.entity.User;
import com.smartDine.services.NotificationService;

/**
 * Controller for managing user notifications.
 */
@RestController
@RequestMapping("/smartdine/api")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get all notifications for the authenticated user.
     * GET /users/me/notifications
     */
    @GetMapping("/me/notifications")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Notification> notifications = notificationService.getUserNotifications(user);
        return ResponseEntity.ok(NotificationDTO.fromEntity(notifications));
    }

    /**
     * Mark a notification as read.
     * POST /notifications/{id}/read
     */
    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<NotificationDTO> markNotificationAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Notification notification = notificationService.markAsRead(id, user);
        return ResponseEntity.ok(NotificationDTO.fromEntity(notification));
    }
}
