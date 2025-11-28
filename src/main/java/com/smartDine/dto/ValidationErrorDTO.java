package com.smartDine.dto;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationErrorDTO extends ErrorDTO {
    private Map<String, String> errors;

    public ValidationErrorDTO(int errorCode, String message, Map<String, String> errors) {
        super(errorCode, message);
        this.errors = errors;
    }
}
