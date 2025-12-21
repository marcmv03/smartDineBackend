package com.smartDine.exceptions;

/**
 * Exception thrown when an invalid reservation status transition is attempted.
 * For example, trying to complete a reservation that is already cancelled,
 * or a customer trying to mark a reservation as completed.
 */
public class IllegalReservationStateChangeException extends RuntimeException {
    
    public IllegalReservationStateChangeException(String message) {
        super(message);
    }
}
