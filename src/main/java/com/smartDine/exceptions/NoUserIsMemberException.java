package com.smartDine.exceptions;

public class NoUserIsMemberException extends RuntimeException {
    public NoUserIsMemberException(String message) {
        super(message);
    }
}
