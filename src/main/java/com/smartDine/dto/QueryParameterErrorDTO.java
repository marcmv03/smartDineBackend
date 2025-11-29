package com.smartDine.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for query parameter validation errors.
 * Extends ErrorDTO to include a list of missing parameter names.
 */
@Getter
@Setter
public class QueryParameterErrorDTO extends ErrorDTO {
    private List<String> missingParams;

    public QueryParameterErrorDTO(int errorCode, String message, List<String> missingParams) {
        super(errorCode, message);
        this.missingParams = missingParams;
    }
}
