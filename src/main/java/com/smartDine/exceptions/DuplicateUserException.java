package com.smartDine.exceptions;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Custom exception for duplicate user registration attempts.
 * Wraps Spring's DataIntegrityViolationException to provide clearer error messaging.
 */
public class DuplicateUserException extends RuntimeException {
    
    private final String field;
    
    public DuplicateUserException(String message, String field) {
        super(message);
        this.field = field;
    }
    
    public DuplicateUserException(String message) {
        super(message);
        this.field = null;
    }
    
    public String getField() {
        return field;
    }
    
    /**
     * Factory method to wrap DataIntegrityViolationException
     */
    public static DuplicateUserException fromDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Ya existe un usuario con este email o número de teléfono";
        
        // Try to determine which field caused the conflict
        String errorMessage = ex.getMessage();
        String field = null;
        
        if (errorMessage != null) {
            if (errorMessage.toLowerCase().contains("email")) {
                field = "email";
                message = "Ya existe un usuario con este email";
            } else if (errorMessage.toLowerCase().contains("phone")) {
                field = "phoneNumber";
                message = "Ya existe un usuario con este número de teléfono";
            }
        }
        
        return new DuplicateUserException(message, field);
    }
}
