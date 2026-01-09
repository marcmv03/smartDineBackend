package com.smartDine.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.entity.Notification;
import com.smartDine.entity.User;
import com.smartDine.repository.NotificationRepository;

@Service
public class NotificationService {

    protected final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Create a notification for a user.
     * 
     * @param receiver The user to receive the notification
     * @param message The notification message
     * @return The created notification
     */
    @Transactional
    public Notification createNotification(User receiver, String message) {
        Notification notification = new Notification(receiver, message);
        return notificationRepository.save(notification);
    }

    /**
     * Get all notifications for a user, ordered by date (newest first).
     * 
     * @param user The user
     * @return List of notifications
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByReceiverOrderByDateDesc(user);
    }

    /**
     * Get unread notifications for a user.
     * 
     * @param user The user
     * @return List of unread notifications
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByReceiverAndReadFalseOrderByDateDesc(user);
    }

    /**
     * Mark a notification as read.
     * 
     * @param notificationId The notification ID
     * @param user The user (must be the receiver)
     * @return The updated notification
     * @throws IllegalArgumentException if notification not found or user is not the receiver
     */
    @Transactional
    public Notification markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + notificationId));

        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not the receiver of this notification");
        }

        notification.markAsRead();
        return notificationRepository.save(notification);
    }
}
