package com.smartDine.dto;

import com.smartDine.entity.RequestType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating a new request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreationDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Request type is required")
    private RequestType requestType;
}
