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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.smartDine.dto.FriendDTO;
import com.smartDine.dto.RequestDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Friendship;
import com.smartDine.entity.Request;
import com.smartDine.entity.RequestStatus;
import com.smartDine.entity.RequestType;
import com.smartDine.entity.Role;
import com.smartDine.exceptions.DuplicateFriendRequestException;
import com.smartDine.exceptions.FriendshipAlreadyExistsException;
import com.smartDine.exceptions.FriendshipNotFoundException;
import com.smartDine.exceptions.NotRequestReceiverException;
import com.smartDine.exceptions.SelfFriendRequestException;
import com.smartDine.services.CustomerService;
import com.smartDine.services.FriendshipRequestService;
import com.smartDine.services.FriendshipService;

@ExtendWith(MockitoExtension.class)
class RequestControllerTest {

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private FriendshipRequestService friendshipRequestService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private RequestController requestController;

    private Customer customer;
    private Customer friend;
    private Business business;
    private Request friendRequest;
    private Friendship friendship;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setEmail("customer@test.com");
        customer.setPhoneNumber(123456789L);

        friend = new Customer();
        friend.setId(2L);
        friend.setName("Test Friend");
        friend.setEmail("friend@test.com");
        friend.setPhoneNumber(987654321L);

        business = new Business();
        business.setId(3L);
        business.setName("Test Business");
        business.setEmail("business@test.com");

        friendRequest = new Request(customer, friend, RequestType.FRIEND_REQUEST);
        friendRequest.setId(10L);

        friendship = new Friendship(customer, friend);
        friendship.setId(20L);
    }

    // ========== Send Request Tests ==========
    @Nested
    @DisplayName("Send Request - POST /users/{id}/requests")
    class SendRequestTests {

        @Test
        @DisplayName("Should return UNAUTHORIZED when user is null")
        void sendRequestUnauthorized() {
            ResponseEntity<RequestDTO> response = requestController.sendRequest(2L, RequestType.FRIEND_REQUEST, null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return FORBIDDEN when user is Business")
        void sendRequestForbiddenForBusiness() {
            ResponseEntity<RequestDTO> response = requestController.sendRequest(2L, RequestType.FRIEND_REQUEST, business);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return CREATED when friend request is sent successfully")
        void sendFriendRequestSuccess() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            when(friendshipRequestService.sendFriendRequest(customer, 2L)).thenReturn(friendRequest);

            ResponseEntity<RequestDTO> response = requestController.sendRequest(2L, RequestType.FRIEND_REQUEST, customer);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(10L, response.getBody().getId());
            verify(friendshipRequestService).sendFriendRequest(customer, 2L);
        }

        @Test
        @DisplayName("Should propagate SelfFriendRequestException")
        void sendFriendRequestToSelf() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            when(friendshipRequestService.sendFriendRequest(customer, 1L))
                    .thenThrow(new SelfFriendRequestException());

            try {
                requestController.sendRequest(1L, RequestType.FRIEND_REQUEST, customer);
            } catch (SelfFriendRequestException e) {
                // Expected
            }
        }

        @Test
        @DisplayName("Should propagate DuplicateFriendRequestException")
        void sendDuplicateFriendRequest() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            when(friendshipRequestService.sendFriendRequest(customer, 2L))
                    .thenThrow(new DuplicateFriendRequestException());

            try {
                requestController.sendRequest(2L, RequestType.FRIEND_REQUEST, customer);
            } catch (DuplicateFriendRequestException e) {
                // Expected
            }
        }

        @Test
        @DisplayName("Should propagate FriendshipAlreadyExistsException")
        void sendFriendRequestWhenAlreadyFriends() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            when(friendshipRequestService.sendFriendRequest(customer, 2L))
                    .thenThrow(new FriendshipAlreadyExistsException());

            try {
                requestController.sendRequest(2L, RequestType.FRIEND_REQUEST, customer);
            } catch (FriendshipAlreadyExistsException e) {
                // Expected
            }
        }
    }

    // ========== Get Pending Requests Tests ==========
    @Nested
    @DisplayName("Get Pending Requests - GET /me/requests")
    class GetPendingRequestsTests {

        @Test
        @DisplayName("Should return UNAUTHORIZED when user is null")
        void getPendingRequestsUnauthorized() {
            ResponseEntity<List<RequestDTO>> response = requestController.getPendingRequests(RequestType.FRIEND_REQUEST, null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return FORBIDDEN when user is Business")
        void getPendingRequestsForbiddenForBusiness() {
            ResponseEntity<List<RequestDTO>> response = requestController.getPendingRequests(RequestType.FRIEND_REQUEST, business);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return empty list when no pending requests")
        void getPendingRequestsEmpty() {
            when(friendshipRequestService.getPendingRequests(customer)).thenReturn(Collections.emptyList());

            ResponseEntity<List<RequestDTO>> response = requestController.getPendingRequests(RequestType.FRIEND_REQUEST, customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("Should return list of pending requests")
        void getPendingRequestsSuccess() {
            Request incomingRequest = new Request(friend, customer, RequestType.FRIEND_REQUEST);
            incomingRequest.setId(11L);
            when(friendshipRequestService.getPendingRequests(customer)).thenReturn(List.of(incomingRequest));

            ResponseEntity<List<RequestDTO>> response = requestController.getPendingRequests(RequestType.FRIEND_REQUEST, customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals(11L, response.getBody().get(0).getId());
        }
    }

    // ========== Accept Request Tests ==========
    @Nested
    @DisplayName("Accept Request - POST /requests/{id}/accept")
    class AcceptRequestTests {

        @Test
        @DisplayName("Should return UNAUTHORIZED when user is null")
        void acceptRequestUnauthorized() {
            ResponseEntity<RequestDTO> response = requestController.acceptRequest(10L, null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return FORBIDDEN when user is Business")
        void acceptRequestForbiddenForBusiness() {
            ResponseEntity<RequestDTO> response = requestController.acceptRequest(10L, business);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return OK when request is accepted")
        void acceptRequestSuccess() {
            Request acceptedRequest = new Request(friend, customer, RequestType.FRIEND_REQUEST);
            acceptedRequest.setId(10L);
            acceptedRequest.accept();
            
            when(friendshipRequestService.acceptRequest(10L, customer)).thenReturn(acceptedRequest);

            ResponseEntity<RequestDTO> response = requestController.acceptRequest(10L, customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(RequestStatus.ACCEPTED, response.getBody().getStatus());
        }

        @Test
        @DisplayName("Should propagate NotRequestReceiverException")
        void acceptRequestNotReceiver() {
            when(friendshipRequestService.acceptRequest(10L, customer))
                    .thenThrow(new NotRequestReceiverException());

            try {
                requestController.acceptRequest(10L, customer);
            } catch (NotRequestReceiverException e) {
                // Expected
            }
        }

        @Test
        @DisplayName("Should propagate IllegalArgumentException when request not found")
        void acceptRequestNotFound() {
            when(friendshipRequestService.acceptRequest(999L, customer))
                    .thenThrow(new IllegalArgumentException("Request not found"));

            try {
                requestController.acceptRequest(999L, customer);
            } catch (IllegalArgumentException e) {
                assertEquals("Request not found", e.getMessage());
            }
        }
    }

    // ========== Reject Request Tests ==========
    @Nested
    @DisplayName("Reject Request - POST /requests/{id}/reject")
    class RejectRequestTests {

        @Test
        @DisplayName("Should return UNAUTHORIZED when user is null")
        void rejectRequestUnauthorized() {
            ResponseEntity<RequestDTO> response = requestController.rejectRequest(10L, null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return FORBIDDEN when user is Business")
        void rejectRequestForbiddenForBusiness() {
            ResponseEntity<RequestDTO> response = requestController.rejectRequest(10L, business);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return OK when request is rejected")
        void rejectRequestSuccess() {
            Request rejectedRequest = new Request(friend, customer, RequestType.FRIEND_REQUEST);
            rejectedRequest.setId(10L);
            rejectedRequest.reject();
            
            when(friendshipRequestService.rejectRequest(10L, customer)).thenReturn(rejectedRequest);

            ResponseEntity<RequestDTO> response = requestController.rejectRequest(10L, customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(RequestStatus.REJECTED, response.getBody().getStatus());
        }

        @Test
        @DisplayName("Should propagate NotRequestReceiverException")
        void rejectRequestNotReceiver() {
            when(friendshipRequestService.rejectRequest(10L, customer))
                    .thenThrow(new NotRequestReceiverException());

            try {
                requestController.rejectRequest(10L, customer);
            } catch (NotRequestReceiverException e) {
                // Expected
            }
        }
    }

    // ========== Get My Friends Tests ==========
    @Nested
    @DisplayName("Get My Friends - GET /me/friends")
    class GetMyFriendsTests {

        @Test
        @DisplayName("Should return UNAUTHORIZED when user is null")
        void getMyFriendsUnauthorized() {
            ResponseEntity<List<FriendDTO>> response = requestController.getMyFriends(null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return FORBIDDEN when user is Business")
        void getMyFriendsForbiddenForBusiness() {
            ResponseEntity<List<FriendDTO>> response = requestController.getMyFriends(business);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return empty list when no friends")
        void getMyFriendsEmpty() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            when(friendshipService.getFriends(customer)).thenReturn(Collections.emptyList());

            ResponseEntity<List<FriendDTO>> response = requestController.getMyFriends(customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("Should return list of friends")
        void getMyFriendsSuccess() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            when(friendshipService.getFriends(customer)).thenReturn(List.of(friendship));

            ResponseEntity<List<FriendDTO>> response = requestController.getMyFriends(customer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals(2L, response.getBody().get(0).getFriendId()); // Friend's ID
            assertEquals("Test Friend", response.getBody().get(0).getFriendName());
        }
    }

    // ========== Remove Friend Tests ==========
    @Nested
    @DisplayName("Remove Friend - DELETE /friends/{friendId}")
    class RemoveFriendTests {

        @Test
        @DisplayName("Should return UNAUTHORIZED when user is null")
        void removeFriendUnauthorized() {
            ResponseEntity<Void> response = requestController.removeFriend(2L, null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return FORBIDDEN when user is Business")
        void removeFriendForbiddenForBusiness() {
            ResponseEntity<Void> response = requestController.removeFriend(2L, business);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return NO_CONTENT when friend is removed")
        void removeFriendSuccess() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            doNothing().when(friendshipService).removeFriend(customer, 2L);

            ResponseEntity<Void> response = requestController.removeFriend(2L, customer);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(friendshipService).removeFriend(customer, 2L);
        }

        @Test
        @DisplayName("Should propagate FriendshipNotFoundException")
        void removeFriendNotFound() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            doThrow(new FriendshipNotFoundException("Not friends"))
                    .when(friendshipService).removeFriend(customer, 999L);

            try {
                requestController.removeFriend(999L, customer);
            } catch (FriendshipNotFoundException e) {
                assertEquals("Not friends", e.getMessage());
            }
        }

        @Test
        @DisplayName("Should propagate IllegalArgumentException when friend user not found")
        void removeFriendUserNotFound() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            doThrow(new IllegalArgumentException("User not found"))
                    .when(friendshipService).removeFriend(customer, 999L);

            try {
                requestController.removeFriend(999L, customer);
            } catch (IllegalArgumentException e) {
                assertEquals("User not found", e.getMessage());
            }
        }
    }
}
