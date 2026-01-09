package com.smartDine.exceptions;

/**
 * Exception thrown when a friendship is not found.
 */
public class FriendshipNotFoundException extends RuntimeException {

    public FriendshipNotFoundException() {
        super("Friendship not found");
    }

    public FriendshipNotFoundException(String message) {
        super(message);
    }
}
