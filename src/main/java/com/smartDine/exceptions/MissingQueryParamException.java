package com.smartDine.exceptions;

import java.util.List;

import org.springframework.web.bind.MissingServletRequestParameterException;

import lombok.Getter;

/**
 * Exception thrown when required query parameters are missing from a request.
 * Wraps Spring's MissingServletRequestParameterException to provide a custom response.
 */
@Getter
public class MissingQueryParamException extends RuntimeException {
    private final List<String> missingParams;

    public MissingQueryParamException(List<String> missingParams) {
        super("Faltan query params requeridos");
        this.missingParams = missingParams;
    }

    public MissingQueryParamException(String message, List<String> missingParams) {
        super(message);
        this.missingParams = missingParams;
    }

    /**
     * Creates a MissingQueryParamException from Spring's MissingServletRequestParameterException
     */
    public static MissingQueryParamException fromSpringException(MissingServletRequestParameterException ex) {
        return new MissingQueryParamException(List.of(ex.getParameterName()));
    }
}
