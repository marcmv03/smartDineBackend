package com.smartDine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a request (friendship, community invite, etc.).
 * Extends Notification - a request IS a notification that can be accepted/rejected.
 * Only one pending request of each type can exist between two users.
 */
@Entity
@Table(name = "requests")
@PrimaryKeyJoinColumn(name = "notification_id")
@DiscriminatorValue("REQUEST")
@Getter
@Setter
@NoArgsConstructor
public class Request extends Notification {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    public Request(User sender, User receiver, RequestType requestType, String message) {
        super(receiver, message);
        this.sender = sender;
        this.requestType = requestType;
        this.status = RequestStatus.PENDING;
    }

    /**
     * Convenience constructor that generates a default message based on request type.
     */
    public Request(User sender, User receiver, RequestType requestType) {
        this(sender, receiver, requestType, generateDefaultMessage(sender, requestType));
    }

    private static String generateDefaultMessage(User sender, RequestType requestType) {
        String senderName = sender != null ? sender.getName() : "Someone";
        if (requestType == RequestType.FRIEND_REQUEST) {
            return senderName + " wants to be your friend";
        } else if (requestType == RequestType.COMMUNITY_INVITE) {
            return senderName + " invited you to join a community";
        }
        return senderName + " sent you a request";
    }

    /**
     * Accept the request. Changes status to ACCEPTED.
     */
    public void accept() {
        this.status = RequestStatus.ACCEPTED;
    }

    /**
     * Reject the request. Changes status to REJECTED.
     */
    public void reject() {
        this.status = RequestStatus.REJECTED;
    }

    /**
     * Check if the request is still pending.
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return this.status == RequestStatus.PENDING;
    }

    @Override
    public String getNotificationType() {
        return "REQUEST";
    }
}
