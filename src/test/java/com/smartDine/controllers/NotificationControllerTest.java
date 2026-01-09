package com.smartDine.controllers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.smartDine.dto.NotificationDTO;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Notification;
import com.smartDine.entity.Request;
import com.smartDine.entity.RequestType;
import com.smartDine.services.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private Customer customer;
    private Notification notification;
    private Request requestNotification;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setEmail("customer@test.com");

        notification = new Notification(customer, "Test notification message");
        notification.setId(10L);

        Customer sender = new Customer();
        sender.setId(2L);
        sender.setName("Sender");

        // Request IS a Notification (inheritance)
        requestNotification = new Request(sender, customer, RequestType.FRIEND_REQUEST);
        requestNotification.setId(20L);
    }

    // ========== Get My Notifications Tests ==========
    @Nested
    @DisplayName("Get My Notifications - GET /me/notifications")
    class GetMyNotificationsTests {

        @Test
        @DisplayName("Should return UNAUTHORIZED when user is null")
        void getNotificationsUnauthorized() {
            ResponseEntity<List<NotificationDTO>> response = notificationController.getMyNotifications(null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return empty list when no notifications")
        void getNotificationsEmpty() {
            when(notificationService.getUserNotifications(customer)).thenReturn(Collections.emptyList());

            ResponseEntity<List<NotificationDTO>> response = notificationController.getMyNotifications(customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("Should return list of notifications including requests")
        void getNotificationsSuccess() {
            // Request extends Notification, so both can be in the list
            when(notificationService.getUserNotifications(customer))
                    .thenReturn(List.of(notification, requestNotification));

            ResponseEntity<List<NotificationDTO>> response = notificationController.getMyNotifications(customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
        }

        @Test
        @DisplayName("Should return notification DTO with request type when notification is a Request")
        void getNotificationsWithRequestType() {
            when(notificationService.getUserNotifications(customer))
                    .thenReturn(List.of(requestNotification));

            ResponseEntity<List<NotificationDTO>> response = notificationController.getMyNotifications(customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            // When the notification is a Request, the DTO should indicate it
            assertEquals("REQUEST", response.getBody().get(0).getNotificationType());
            assertEquals(RequestType.FRIEND_REQUEST, response.getBody().get(0).getRequestType());
        }

        @Test
        @DisplayName("Should return regular notification without request type")
        void getNotificationsWithoutRequestType() {
            when(notificationService.getUserNotifications(customer))
                    .thenReturn(List.of(notification));

            ResponseEntity<List<NotificationDTO>> response = notificationController.getMyNotifications(customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals("NOTIFICATION", response.getBody().get(0).getNotificationType());
            assertEquals(null, response.getBody().get(0).getRequestType());
        }
    }

    // ========== Mark Notification As Read Tests ==========
    @Nested
    @DisplayName("Mark Notification As Read - POST /notifications/{id}/read")
    class MarkNotificationAsReadTests {

        @Test
        @DisplayName("Should return UNAUTHORIZED when user is null")
        void markAsReadUnauthorized() {
            ResponseEntity<NotificationDTO> response = notificationController.markNotificationAsRead(10L, null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return OK when notification is marked as read")
        void markAsReadSuccess() {
            Notification readNotification = new Notification(customer, "Test message");
            readNotification.setId(10L);
            readNotification.markAsRead();

            when(notificationService.markAsRead(10L, customer)).thenReturn(readNotification);

            ResponseEntity<NotificationDTO> response = notificationController.markNotificationAsRead(10L, customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isRead());
            verify(notificationService).markAsRead(10L, customer);
        }

        @Test
        @DisplayName("Should return OK when Request notification is marked as read")
        void markRequestAsReadSuccess() {
            requestNotification.markAsRead();
            when(notificationService.markAsRead(20L, customer)).thenReturn(requestNotification);

            ResponseEntity<NotificationDTO> response = notificationController.markNotificationAsRead(20L, customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isRead());
            assertEquals("REQUEST", response.getBody().getNotificationType());
        }

        @Test
        @DisplayName("Should propagate IllegalArgumentException when notification not found")
        void markAsReadNotFound() {
            when(notificationService.markAsRead(999L, customer))
                    .thenThrow(new IllegalArgumentException("Notification not found"));

            try {
                notificationController.markNotificationAsRead(999L, customer);
            } catch (IllegalArgumentException e) {
                assertEquals("Notification not found", e.getMessage());
            }
        }

        @Test
        @DisplayName("Should propagate IllegalArgumentException when user is not the receiver")
        void markAsReadNotReceiver() {
            Customer otherCustomer = new Customer();
            otherCustomer.setId(99L);

            when(notificationService.markAsRead(10L, otherCustomer))
                    .thenThrow(new IllegalArgumentException("You cannot mark this notification as read"));

            try {
                notificationController.markNotificationAsRead(10L, otherCustomer);
            } catch (IllegalArgumentException e) {
                assertEquals("You cannot mark this notification as read", e.getMessage());
            }
        }
    }
}
