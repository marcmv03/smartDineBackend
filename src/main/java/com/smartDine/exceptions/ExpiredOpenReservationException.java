package com.smartDine.exceptions;

/**
 * Exception thrown when attempting to join an open reservation post
 * where the reservation date has already passed.
 */
public class ExpiredOpenReservationException extends RuntimeException {
    
    public ExpiredOpenReservationException(String message) {
        super(message);
    }
}
