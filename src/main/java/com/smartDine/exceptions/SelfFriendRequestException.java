package com.smartDine.exceptions;

/**
 * Exception thrown when a user tries to send a friend request to themselves.
 */
public class SelfFriendRequestException extends RuntimeException {

    public SelfFriendRequestException() {
        super("You cannot send a friend request to yourself");
    }

    public SelfFriendRequestException(String message) {
        super(message);
    }
}
