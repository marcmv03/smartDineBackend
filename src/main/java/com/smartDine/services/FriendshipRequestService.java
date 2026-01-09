package com.smartDine.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.entity.Customer;
import com.smartDine.entity.Request;
import com.smartDine.entity.RequestStatus;
import com.smartDine.entity.RequestType;
import com.smartDine.exceptions.DuplicateFriendRequestException;
import com.smartDine.exceptions.FriendshipAlreadyExistsException;
import com.smartDine.exceptions.SelfFriendRequestException;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.NotificationRepository;
import com.smartDine.repository.RequestRepository;

/**
 * Service for managing friendship requests.
 * Extends RequestService and implements specific logic for friend requests.
 */
@Service
public class FriendshipRequestService extends RequestService {

    private final CustomerRepository customerRepository;
    private final FriendshipService friendshipService;

    public FriendshipRequestService(
            NotificationRepository notificationRepository,
            RequestRepository requestRepository,
            CustomerRepository customerRepository,
            FriendshipService friendshipService) {
        super(notificationRepository, requestRepository);
        this.customerRepository = customerRepository;
        this.friendshipService = friendshipService;
    }

    @Override
    protected RequestType getRequestType() {
        return RequestType.FRIEND_REQUEST;
    }

    /**
     * Called when a friend request is accepted.
     * Creates the friendship between sender and receiver.
     * @param request The accepted request
     */
    @Override
    protected void onAccept(Request request) {
        Customer sender = customerRepository.findById(request.getSender().getId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        Customer receiver = customerRepository.findById(request.getReceiver().getId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));
        
        friendshipService.createFriendship(sender, receiver);
        
        // Create a notification for the sender that their request was accepted
        String message = receiver.getName() + " ha aceptado tu solicitud de amistad";
        createNotification(sender, message);
    }

    /**
     * Called when a friend request is rejected.
     * No action needed for rejections.
     * @param request The rejected request
     */
    @Override
    protected void onReject(Request request) {
        // No action needed on rejection for friend requests
    }

    /**
     * Send a friend request from one customer to another.
     * 
     * @param sender The customer sending the request
     * @param receiverId The ID of the customer to receive the request
     * @return The created request
     * @throws SelfFriendRequestException if sender tries to send to themselves
     * @throws IllegalArgumentException if receiver not found or is not a Customer
     * @throws DuplicateFriendRequestException if a pending request already exists
     * @throws FriendshipAlreadyExistsException if they are already friends
     */
    @Transactional
    public Request sendFriendRequest(Customer sender, Long receiverId) {
        // Validate: no self-request
        if (sender.getId().equals(receiverId)) {
            throw new SelfFriendRequestException();
        }

        // Validate: receiver exists and is a Customer
        Customer receiver = customerRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + receiverId));

        // Validate: not already friends
        if (friendshipService.areFriends(sender, receiver)) {
            throw new FriendshipAlreadyExistsException();
        }

        // Validate: no duplicate pending request (in either direction)
        boolean pendingExists = requestRepository.existsBySenderAndReceiverAndRequestTypeAndStatus(
                sender, receiver, RequestType.FRIEND_REQUEST, RequestStatus.PENDING);
        boolean reversePendingExists = requestRepository.existsBySenderAndReceiverAndRequestTypeAndStatus(
                receiver, sender, RequestType.FRIEND_REQUEST, RequestStatus.PENDING);

        if (pendingExists || reversePendingExists) {
            throw new DuplicateFriendRequestException();
        }

        // Create the message for the notification
        String message = sender.getName() + " te ha enviado una solicitud de amistad";

        // Create and save the request (which is also a notification)
        Request request = new Request(sender, receiver, RequestType.FRIEND_REQUEST, message);
        return requestRepository.save(request);
    }

    /**
     * Get pending friend requests for a user.
     * Alias for getPendingRequests().
     * @param user The user
     * @return List of pending friend requests
     */
    @Transactional(readOnly = true)
    public List<Request> getPendingFriendRequests(com.smartDine.entity.User user) {
        return getPendingRequests(user);
    }
}
