package com.smartDine.services;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.smartDine.entity.Request;
import com.smartDine.entity.RequestStatus;
import com.smartDine.entity.RequestType;
import com.smartDine.entity.User;
import com.smartDine.exceptions.NotRequestReceiverException;
import com.smartDine.repository.NotificationRepository;
import com.smartDine.repository.RequestRepository;

/**
 * Abstract service for managing requests.
 * Extends NotificationService since Request extends Notification.
 * Subclasses implement onAccept() and onReject() for specific request types.
 */
public abstract class RequestService extends NotificationService {

    protected final RequestRepository requestRepository;

    public RequestService(
            NotificationRepository notificationRepository,
            RequestRepository requestRepository) {
        super(notificationRepository);
        this.requestRepository = requestRepository;
    }

    /**
     * Get the request type this service handles.
     * @return The RequestType this service manages
     */
    protected abstract RequestType getRequestType();

    /**
     * Called when a request is accepted.
     * Subclasses implement the specific logic (e.g., create friendship).
     * @param request The accepted request
     */
    protected abstract void onAccept(Request request);

    /**
     * Called when a request is rejected.
     * Subclasses implement the specific logic if needed.
     * @param request The rejected request
     */
    protected abstract void onReject(Request request);

    /**
     * Get pending requests of this type for a user.
     * 
     * @param user The user (receiver)
     * @return List of pending requests
     */
    @Transactional(readOnly = true)
    public List<Request> getPendingRequests(User user) {
        return requestRepository.findByReceiverAndRequestTypeAndStatusOrderByDateDesc(
                user, getRequestType(), RequestStatus.PENDING);
    }

    /**
     * Accept a request.
     * 
     * @param requestId The request ID
     * @param user The user accepting (must be the receiver)
     * @return The updated request
     * @throws IllegalArgumentException if request not found or not pending
     * @throws NotRequestReceiverException if user is not the receiver
     */
    @Transactional
    public Request acceptRequest(Long requestId, User user) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));

        validateReceiverAndPending(request, user);

        // Accept the request
        request.accept();
        request = requestRepository.save(request);

        // Call subclass-specific logic
        onAccept(request);

        return request;
    }

    /**
     * Reject a request.
     * 
     * @param requestId The request ID
     * @param user The user rejecting (must be the receiver)
     * @return The updated request
     * @throws IllegalArgumentException if request not found or not pending
     * @throws NotRequestReceiverException if user is not the receiver
     */
    @Transactional
    public Request rejectRequest(Long requestId, User user) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));

        validateReceiverAndPending(request, user);

        // Reject the request
        request.reject();
        request = requestRepository.save(request);

        // Call subclass-specific logic
        onReject(request);

        return request;
    }

    /**
     * Validate that the user is the receiver and the request is pending.
     */
    protected void validateReceiverAndPending(Request request, User user) {
        if (!request.getReceiver().getId().equals(user.getId())) {
            throw new NotRequestReceiverException();
        }
        if (!request.isPending()) {
            throw new IllegalArgumentException("Request is not pending");
        }
    }
}
