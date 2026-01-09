package com.smartDine.exceptions;

/**
 * Exception thrown when trying to create a friendship that already exists.
 */
public class FriendshipAlreadyExistsException extends RuntimeException {

    public FriendshipAlreadyExistsException() {
        super("You are already friends with this user");
    }

    public FriendshipAlreadyExistsException(String message) {
        super(message);
    }
}
