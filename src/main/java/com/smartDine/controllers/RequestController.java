package com.smartDine.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.FriendDTO;
import com.smartDine.dto.RequestDTO;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Friendship;
import com.smartDine.entity.Request;
import com.smartDine.entity.RequestType;
import com.smartDine.entity.Role;
import com.smartDine.entity.User;
import com.smartDine.services.CustomerService;
import com.smartDine.services.FriendshipRequestService;
import com.smartDine.services.FriendshipService;
import com.smartDine.services.RequestService;

/**
 * Controller for managing requests (friend requests, community invites, etc.).
 * Delegates to specific RequestService implementations based on request type.
 * 
 * Endpoints:
 * - POST /users/{id}/requests?type=FRIEND_REQUEST - Send a request
 * - GET /me/requests?type=FRIEND_REQUEST - Get pending requests of a type
 * - POST /requests/{id}/accept - Accept a request
 * - POST /requests/{id}/reject - Reject a request
 * - GET /me/friends - Get friends list
 * - DELETE /friends/{friendId} - Remove a friend
 */
@RestController
@RequestMapping("/smartdine/api")
@CrossOrigin(origins = "*")
public class RequestController {

    private final FriendshipService friendshipService;
    private final FriendshipRequestService friendshipRequestService;
    private final CustomerService customerService;

    public RequestController(
            FriendshipService friendshipService,
            FriendshipRequestService friendshipRequestService,
            CustomerService customerService) {
        this.friendshipService = friendshipService;
        this.friendshipRequestService = friendshipRequestService;
        this.customerService = customerService;
    }

    /**
     * Get the appropriate RequestService for a given type.
     * Extend this method when adding new request types.
     * 
     * @param type The request type
     * @return The appropriate RequestService implementation
     */
    private RequestService getServiceForType(RequestType type) {
        if (type == RequestType.FRIEND_REQUEST) {
            return friendshipRequestService;
        } else if (type == RequestType.COMMUNITY_INVITE) {
            throw new IllegalArgumentException("Community invites not yet implemented");
        }
        throw new IllegalArgumentException("Unknown request type: " + type);
    }

    /**
     * Send a request to another user.
     * POST /users/{id}/requests?type=FRIEND_REQUEST
     * 
     * @param id The target user ID
     * @param type The type of request to send (defaults to FRIEND_REQUEST)
     * @param user The authenticated user
     * @return The created request
     */
    @PostMapping("/users/{id}/requests")
    public ResponseEntity<RequestDTO> sendRequest(
            @PathVariable Long id,
            @RequestParam(defaultValue = "FRIEND_REQUEST") RequestType type,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Customer sender = customerService.getCustomerById(user.getId());
        
        Request request;
        if (type == RequestType.FRIEND_REQUEST) {
            request = friendshipRequestService.sendFriendRequest(sender, id);
        } else {
            throw new IllegalArgumentException("Request type not supported: " + type);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(RequestDTO.fromEntity(request));
    }

    /**
     * Get pending requests received by the authenticated user.
     * GET /me/requests?type=FRIEND_REQUEST
     * 
     * @param type The type of requests to retrieve (defaults to FRIEND_REQUEST)
     * @param user The authenticated user
     * @return List of pending requests
     */
    @GetMapping("/me/requests")
    public ResponseEntity<List<RequestDTO>> getPendingRequests(
            @RequestParam(defaultValue = "FRIEND_REQUEST") RequestType type,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        RequestService service = getServiceForType(type);
        List<Request> requests = service.getPendingRequests(user);
        return ResponseEntity.ok(RequestDTO.fromEntity(requests));
    }

    /**
     * Accept a request.
     * POST /requests/{id}/accept
     * 
     * The request type is determined from the request itself.
     * 
     * @param id The request ID
     * @param user The authenticated user
     * @return The updated request
     */
    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<RequestDTO> acceptRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // For now, we use friendshipRequestService directly
        // In the future, we could look up the request first to determine its type
        Request request = friendshipRequestService.acceptRequest(id, user);
        return ResponseEntity.ok(RequestDTO.fromEntity(request));
    }

    /**
     * Reject a request.
     * POST /requests/{id}/reject
     * 
     * @param id The request ID
     * @param user The authenticated user
     * @return The updated request
     */
    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<RequestDTO> rejectRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Request request = friendshipRequestService.rejectRequest(id, user);
        return ResponseEntity.ok(RequestDTO.fromEntity(request));
    }

    /**
     * Get the list of friends for the authenticated user.
     * GET /me/friends
     */
    @GetMapping("/me/friends")
    public ResponseEntity<List<FriendDTO>> getMyFriends(
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Customer customer = customerService.getCustomerById(user.getId());
        List<Friendship> friendships = friendshipService.getFriends(customer);
        return ResponseEntity.ok(FriendDTO.fromEntity(friendships, customer));
    }

    /**
     * Remove a friend.
     * DELETE /friends/{friendId}
     */
    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long friendId,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Customer customer = customerService.getCustomerById(user.getId());
        friendshipService.removeFriend(customer, friendId);
        return ResponseEntity.noContent().build();
    }
}
