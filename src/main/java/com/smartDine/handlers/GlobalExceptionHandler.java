package com.smartDine.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.smartDine.dto.ErrorDTO;
import com.smartDine.dto.QueryParameterErrorDTO;
import com.smartDine.dto.ValidationErrorDTO;
import com.smartDine.exceptions.DuplicateFriendRequestException;
import com.smartDine.exceptions.DuplicateUserException;
import com.smartDine.exceptions.ExpiredOpenReservationException;
import com.smartDine.exceptions.FriendshipAlreadyExistsException;
import com.smartDine.exceptions.FriendshipNotFoundException;
import com.smartDine.exceptions.IllegalReservationStateChangeException;
import com.smartDine.exceptions.MissingQueryParamException;
import com.smartDine.exceptions.NoUserIsMemberException;
import com.smartDine.exceptions.NotRequestReceiverException;
import com.smartDine.exceptions.RelatedEntityException;
import com.smartDine.exceptions.SelfFriendRequestException;

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

    @ExceptionHandler(MissingQueryParamException.class)
    public ResponseEntity<QueryParameterErrorDTO> handleMissingQueryParamException(MissingQueryParamException ex) {
        QueryParameterErrorDTO errorDTO = new QueryParameterErrorDTO(
            HttpStatus.BAD_REQUEST.value(),
            "Faltan query params requeridos",
            ex.getMissingParams()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Spring's MissingServletRequestParameterException
     * This is thrown when @RequestParam(required=true) parameters are missing
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<QueryParameterErrorDTO> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex) {
        QueryParameterErrorDTO errorDTO = new QueryParameterErrorDTO(
            HttpStatus.BAD_REQUEST.value(),
            "Faltan query params requeridos",
            List.of(ex.getParameterName())
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle duplicate user registration attempts
     */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorDTO> handleDuplicateUserException(DuplicateUserException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.CONFLICT.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }

    /**
     * Handle user not being a member or lacking permissions
     */
    @ExceptionHandler(NoUserIsMemberException.class)
    public ResponseEntity<ErrorDTO> handleNoUserIsMemberException(NoUserIsMemberException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.CONFLICT.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }

    /**
     * Handle illegal reservation state change (409 Conflict)
     */
    @ExceptionHandler(IllegalReservationStateChangeException.class)
    public ResponseEntity<ErrorDTO> handleIllegalReservationStateChange(IllegalReservationStateChangeException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.CONFLICT.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }

    /**
     * Handle expired open reservation (409 Conflict)
     * Thrown when attempting to join an open reservation where the date has passed
     */
    @ExceptionHandler(ExpiredOpenReservationException.class)
    public ResponseEntity<ErrorDTO> handleExpiredOpenReservation(ExpiredOpenReservationException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.CONFLICT.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }

    /**
     * Handle self friend request (409 Conflict)
     * Thrown when a user tries to send a friend request to themselves
     */
    @ExceptionHandler(SelfFriendRequestException.class)
    public ResponseEntity<ErrorDTO> handleSelfFriendRequest(SelfFriendRequestException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.CONFLICT.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }

    /**
     * Handle duplicate friend request (409 Conflict)
     * Thrown when a pending friend request already exists
     */
    @ExceptionHandler(DuplicateFriendRequestException.class)
    public ResponseEntity<ErrorDTO> handleDuplicateFriendRequest(DuplicateFriendRequestException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.CONFLICT.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }

    /**
     * Handle friendship already exists (409 Conflict)
     * Thrown when trying to befriend someone who is already a friend
     */
    @ExceptionHandler(FriendshipAlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleFriendshipAlreadyExists(FriendshipAlreadyExistsException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.CONFLICT.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }

    /**
     * Handle not request receiver (403 Forbidden)
     * Thrown when a user tries to accept/reject a request they are not the receiver of
     */
    @ExceptionHandler(NotRequestReceiverException.class)
    public ResponseEntity<ErrorDTO> handleNotRequestReceiver(NotRequestReceiverException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.FORBIDDEN.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle friendship not found (404 Not Found)
     * Thrown when trying to remove a friendship that doesn't exist
     */
    @ExceptionHandler(FriendshipNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleFriendshipNotFound(FriendshipNotFoundException ex) {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }
}