package com.smartDine.services;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartDine.entity.Customer;
import com.smartDine.entity.Notification;
import com.smartDine.entity.Request;
import com.smartDine.entity.RequestType;
import com.smartDine.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Customer user;
    private Customer sender;
    private Notification notification;
    private Request friendRequest;

    @BeforeEach
    void setUp() {
        user = new Customer();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("user@test.com");

        sender = new Customer();
        sender.setId(2L);
        sender.setName("Sender");
        sender.setEmail("sender@test.com");

        notification = new Notification(user, "Test notification");
        notification.setId(10L);

        // Request now extends Notification, so a Request IS a Notification
        friendRequest = new Request(sender, user, RequestType.FRIEND_REQUEST);
        friendRequest.setId(20L);
    }

    // ========== Create Notification Tests ==========
    @Nested
    @DisplayName("createNotification")
    class CreateNotificationTests {

        @Test
        @DisplayName("Should create notification")
        void createNotification() {
            when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setId(30L);
                return n;
            });

            Notification result = notificationService.createNotification(user, "New message");

            assertNotNull(result);
            assertEquals(30L, result.getId());
            assertEquals("New message", result.getMessage());
            assertEquals(user, result.getReceiver());
        }
    }

    // ========== Get User Notifications Tests ==========
    @Nested
    @DisplayName("getUserNotifications")
    class GetUserNotificationsTests {

        @Test
        @DisplayName("Should return empty list when no notifications")
        void getNotificationsEmpty() {
            when(notificationRepository.findByReceiverOrderByDateDesc(user)).thenReturn(List.of());

            List<Notification> result = notificationService.getUserNotifications(user);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return list of notifications ordered by date")
        void getNotificationsSuccess() {
            Notification older = new Notification(user, "Older notification");
            older.setId(12L);

            when(notificationRepository.findByReceiverOrderByDateDesc(user))
                    .thenReturn(List.of(notification, older));

            List<Notification> result = notificationService.getUserNotifications(user);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should include Request notifications in the list")
        void getNotificationsIncludesRequests() {
            // Request extends Notification, so it should be included
            when(notificationRepository.findByReceiverOrderByDateDesc(user))
                    .thenReturn(List.of(notification, friendRequest));

            List<Notification> result = notificationService.getUserNotifications(user);

            assertEquals(2, result.size());
            // One is a regular notification, one is a Request
            assertTrue(result.stream().anyMatch(n -> n instanceof Request));
        }
    }

    // ========== Get Unread Notifications Tests ==========
    @Nested
    @DisplayName("getUnreadNotifications")
    class GetUnreadNotificationsTests {

        @Test
        @DisplayName("Should return only unread notifications")
        void getUnreadNotifications() {
            when(notificationRepository.findByReceiverAndReadFalseOrderByDateDesc(user))
                    .thenReturn(List.of(notification));

            List<Notification> result = notificationService.getUnreadNotifications(user);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return empty list when all read")
        void getUnreadNotificationsEmpty() {
            when(notificationRepository.findByReceiverAndReadFalseOrderByDateDesc(user))
                    .thenReturn(List.of());

            List<Notification> result = notificationService.getUnreadNotifications(user);

            assertTrue(result.isEmpty());
        }
    }

    // ========== Mark As Read Tests ==========
    @Nested
    @DisplayName("markAsRead")
    class MarkAsReadTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when notification not found")
        void markAsReadNotFound() {
            when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                notificationService.markAsRead(999L, user);
            });

            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user is not the receiver")
        void markAsReadNotReceiver() {
            Customer otherUser = new Customer();
            otherUser.setId(99L);
            otherUser.setName("Other User");

            when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                notificationService.markAsRead(10L, otherUser);
            });

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Should mark notification as read successfully")
        void markAsReadSuccess() {
            when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Notification result = notificationService.markAsRead(10L, user);

            assertTrue(result.isRead());
            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("Should mark Request as read successfully")
        void markRequestAsReadSuccess() {
            when(notificationRepository.findById(20L)).thenReturn(Optional.of(friendRequest));
            when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Notification result = notificationService.markAsRead(20L, user);

            assertTrue(result.isRead());
            verify(notificationRepository).save(friendRequest);
        }
    }
}
