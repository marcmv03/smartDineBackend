package com.smartDine.exceptions;

/**
 * Exception thrown when a user tries to accept/reject a request they are not the receiver of.
 */
public class NotRequestReceiverException extends RuntimeException {

    public NotRequestReceiverException() {
        super("You are not the receiver of this request");
    }

    public NotRequestReceiverException(String message) {
        super(message);
    }
}
