package com.smartDine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Notification;
import com.smartDine.entity.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a user, ordered by date descending (newest first).
     */
    List<Notification> findByReceiverOrderByDateDesc(User receiver);

    /**
     * Find all unread notifications for a user, ordered by date descending.
     */
    List<Notification> findByReceiverAndReadFalseOrderByDateDesc(User receiver);

    /**
     * Count unread notifications for a user.
     */
    long countByReceiverAndReadFalse(User receiver);
}
