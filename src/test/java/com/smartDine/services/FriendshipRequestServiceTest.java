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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartDine.entity.Customer;
import com.smartDine.entity.Request;
import com.smartDine.entity.RequestStatus;
import com.smartDine.entity.RequestType;
import com.smartDine.exceptions.DuplicateFriendRequestException;
import com.smartDine.exceptions.FriendshipAlreadyExistsException;
import com.smartDine.exceptions.NotRequestReceiverException;
import com.smartDine.exceptions.SelfFriendRequestException;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.NotificationRepository;
import com.smartDine.repository.RequestRepository;

@ExtendWith(MockitoExtension.class)
class FriendshipRequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private FriendshipRequestService friendshipRequestService;

    private Customer sender;
    private Customer receiver;
    private Request pendingRequest;

    @BeforeEach
    void setUp() {
        sender = new Customer();
        sender.setId(1L);
        sender.setName("Sender");
        sender.setEmail("sender@test.com");

        receiver = new Customer();
        receiver.setId(2L);
        receiver.setName("Receiver");
        receiver.setEmail("receiver@test.com");

        pendingRequest = new Request(sender, receiver, RequestType.FRIEND_REQUEST);
        pendingRequest.setId(10L);
    }

    // ========== Send Friend Request Tests ==========
    @Nested
    @DisplayName("sendFriendRequest")
    class SendFriendRequestTests {

        @Test
        @DisplayName("Should throw SelfFriendRequestException when sending to self")
        void sendFriendRequestToSelf() {
            assertThrows(SelfFriendRequestException.class, () -> {
                friendshipRequestService.sendFriendRequest(sender, sender.getId());
            });

            verify(requestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when receiver not found")
        void sendFriendRequestReceiverNotFound() {
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                friendshipRequestService.sendFriendRequest(sender, 999L);
            });

            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Should throw FriendshipAlreadyExistsException when already friends")
        void sendFriendRequestAlreadyFriends() {
            when(customerRepository.findById(2L)).thenReturn(Optional.of(receiver));
            when(friendshipService.areFriends(sender, receiver)).thenReturn(true);

            assertThrows(FriendshipAlreadyExistsException.class, () -> {
                friendshipRequestService.sendFriendRequest(sender, 2L);
            });

            verify(requestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateFriendRequestException when pending request exists")
        void sendFriendRequestDuplicate() {
            when(customerRepository.findById(2L)).thenReturn(Optional.of(receiver));
            when(friendshipService.areFriends(sender, receiver)).thenReturn(false);
            when(requestRepository.existsBySenderAndReceiverAndRequestTypeAndStatus(
                    sender, receiver, RequestType.FRIEND_REQUEST, RequestStatus.PENDING))
                    .thenReturn(true);

            assertThrows(DuplicateFriendRequestException.class, () -> {
                friendshipRequestService.sendFriendRequest(sender, 2L);
            });
        }

        @Test
        @DisplayName("Should throw DuplicateFriendRequestException when reverse pending request exists")
        void sendFriendRequestReverseDuplicate() {
            when(customerRepository.findById(2L)).thenReturn(Optional.of(receiver));
            when(friendshipService.areFriends(sender, receiver)).thenReturn(false);
            when(requestRepository.existsBySenderAndReceiverAndRequestTypeAndStatus(
                    sender, receiver, RequestType.FRIEND_REQUEST, RequestStatus.PENDING))
                    .thenReturn(false);
            when(requestRepository.existsBySenderAndReceiverAndRequestTypeAndStatus(
                    receiver, sender, RequestType.FRIEND_REQUEST, RequestStatus.PENDING))
                    .thenReturn(true);

            assertThrows(DuplicateFriendRequestException.class, () -> {
                friendshipRequestService.sendFriendRequest(sender, 2L);
            });
        }

        @Test
        @DisplayName("Should create and save friend request successfully")
        void sendFriendRequestSuccess() {
            when(customerRepository.findById(2L)).thenReturn(Optional.of(receiver));
            when(friendshipService.areFriends(sender, receiver)).thenReturn(false);
            when(requestRepository.existsBySenderAndReceiverAndRequestTypeAndStatus(
                    sender, receiver, RequestType.FRIEND_REQUEST, RequestStatus.PENDING))
                    .thenReturn(false);
            when(requestRepository.existsBySenderAndReceiverAndRequestTypeAndStatus(
                    receiver, sender, RequestType.FRIEND_REQUEST, RequestStatus.PENDING))
                    .thenReturn(false);
            when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
                Request req = invocation.getArgument(0);
                req.setId(10L);
                return req;
            });

            Request result = friendshipRequestService.sendFriendRequest(sender, 2L);

            assertNotNull(result);
            assertEquals(sender, result.getSender());
            assertEquals(receiver, result.getReceiver());
            assertEquals(RequestType.FRIEND_REQUEST, result.getRequestType());
            assertEquals(RequestStatus.PENDING, result.getStatus());
        }
    }

    // ========== Get Pending Friend Requests Tests ==========
    @Nested
    @DisplayName("getPendingFriendRequests")
    class GetPendingFriendRequestsTests {

        @Test
        @DisplayName("Should return empty list when no pending requests")
        void getPendingRequestsEmpty() {
            when(requestRepository.findByReceiverAndRequestTypeAndStatusOrderByDateDesc(
                    receiver, RequestType.FRIEND_REQUEST, RequestStatus.PENDING))
                    .thenReturn(List.of());

            List<Request> result = friendshipRequestService.getPendingFriendRequests(receiver);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return list of pending requests")
        void getPendingRequestsSuccess() {
            Request request1 = new Request(sender, receiver, RequestType.FRIEND_REQUEST);
            request1.setId(11L);
            
            Customer anotherSender = new Customer();
            anotherSender.setId(3L);
            anotherSender.setName("Another Sender");
            Request request2 = new Request(anotherSender, receiver, RequestType.FRIEND_REQUEST);
            request2.setId(12L);

            when(requestRepository.findByReceiverAndRequestTypeAndStatusOrderByDateDesc(
                    receiver, RequestType.FRIEND_REQUEST, RequestStatus.PENDING))
                    .thenReturn(List.of(request1, request2));

            List<Request> result = friendshipRequestService.getPendingFriendRequests(receiver);

            assertEquals(2, result.size());
        }
    }

    // ========== Accept Request Tests ==========
    @Nested
    @DisplayName("acceptRequest")
    class AcceptRequestTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when request not found")
        void acceptRequestNotFound() {
            when(requestRepository.findById(999L)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                friendshipRequestService.acceptRequest(999L, receiver);
            });

            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Should throw NotRequestReceiverException when user is not the receiver")
        void acceptRequestNotReceiver() {
            when(requestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));

            assertThrows(NotRequestReceiverException.class, () -> {
                friendshipRequestService.acceptRequest(10L, sender); // sender is not the receiver
            });
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when request is not pending")
        void acceptRequestNotPending() {
            Request acceptedRequest = new Request(sender, receiver, RequestType.FRIEND_REQUEST);
            acceptedRequest.setId(10L);
            acceptedRequest.accept();

            when(requestRepository.findById(10L)).thenReturn(Optional.of(acceptedRequest));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                friendshipRequestService.acceptRequest(10L, receiver);
            });

            assertTrue(exception.getMessage().contains("not pending"));
        }

        @Test
        @DisplayName("Should accept request and create friendship")
        void acceptRequestSuccess() {
            when(requestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
            when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sender));
            when(customerRepository.findById(2L)).thenReturn(Optional.of(receiver));

            Request result = friendshipRequestService.acceptRequest(10L, receiver);

            assertEquals(RequestStatus.ACCEPTED, result.getStatus());
            verify(friendshipService).createFriendship(sender, receiver);
            // Verify notification is created
            verify(notificationRepository).save(any());
        }
    }

    // ========== Reject Request Tests ==========
    @Nested
    @DisplayName("rejectRequest")
    class RejectRequestTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when request not found")
        void rejectRequestNotFound() {
            when(requestRepository.findById(999L)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                friendshipRequestService.rejectRequest(999L, receiver);
            });

            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Should throw NotRequestReceiverException when user is not the receiver")
        void rejectRequestNotReceiver() {
            when(requestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));

            assertThrows(NotRequestReceiverException.class, () -> {
                friendshipRequestService.rejectRequest(10L, sender); // sender is not the receiver
            });
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when request is not pending")
        void rejectRequestNotPending() {
            Request rejectedRequest = new Request(sender, receiver, RequestType.FRIEND_REQUEST);
            rejectedRequest.setId(10L);
            rejectedRequest.reject();

            when(requestRepository.findById(10L)).thenReturn(Optional.of(rejectedRequest));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                friendshipRequestService.rejectRequest(10L, receiver);
            });

            assertTrue(exception.getMessage().contains("not pending"));
        }

        @Test
        @DisplayName("Should reject request successfully")
        void rejectRequestSuccess() {
            when(requestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
            when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Request result = friendshipRequestService.rejectRequest(10L, receiver);

            assertEquals(RequestStatus.REJECTED, result.getStatus());
            verify(friendshipService, never()).createFriendship(any(), any());
        }
    }
}
