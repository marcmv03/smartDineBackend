package com.smartDine.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.smartDine.dto.ErrorDTO;
import com.smartDine.dto.ValidationErrorDTO;
import com.smartDine.exceptions.RelatedEntityException;

import io.jsonwebtoken.ExpiredJwtException; 

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors for @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ValidationErrorDTO errorDTO = new ValidationErrorDTO(
            HttpStatus.BAD_REQUEST.value(),
            "Errores de validación",
            errors
        );
        
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle business logic exceptions (IllegalArgumentException)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(BadCredentialsException.class) 
    public ResponseEntity<ErrorDTO> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getMessage()
        );
        
        return new ResponseEntity<>(errorDTO, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDTO> handleNoResourceFoundException(NoResourceFoundException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.NOT_FOUND.value(),
            "The requested resource was not found"
        );
        
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class) 
    public ResponseEntity<ErrorDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.BAD_REQUEST.value(),
            "Malformed JSON request"
        );
        
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class) 
    public ResponseEntity<ErrorDTO> handleIOException(IOException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error de entrada/salida: " + ex.getMessage()
        );
        
        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorDTO> handleExpiredJwtException(ExpiredJwtException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.UNAUTHORIZED.value(),
            "El token ha expirado. Por favor, inicie sesión de nuevo."
        );
        
        return new ResponseEntity<>(errorDTO, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDTO> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            "HTTP method not supported: " + ex.getMethod()
        );
        
        return new ResponseEntity<>(errorDTO, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGeneralException(Exception ex) {
        // Log the actual error for debugging
        System.err.println("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();
        
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Ha ocurrido un error interno del servidor"
        );
        
        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(RelatedEntityException.class)
    public ResponseEntity<ErrorDTO> handleRelatedEntityException(RelatedEntityException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.CONFLICT.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);

}
}