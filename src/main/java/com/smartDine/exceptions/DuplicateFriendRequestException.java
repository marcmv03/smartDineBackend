package com.smartDine.exceptions;

/**
 * Exception thrown when a duplicate friend request already exists.
 */
public class DuplicateFriendRequestException extends RuntimeException {

    public DuplicateFriendRequestException() {
        super("A pending friend request already exists between these users");
    }

    public DuplicateFriendRequestException(String message) {
        super(message);
    }
}
