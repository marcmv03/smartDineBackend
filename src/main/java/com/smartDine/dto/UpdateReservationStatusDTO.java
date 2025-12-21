package com.smartDine.dto;

import com.smartDine.entity.ReservationStatus;

import jakarta.validation.constraints.NotNull;

public class UpdateReservationStatusDTO {
    
    @NotNull(message = "Status is required")
    private ReservationStatus status;

    public UpdateReservationStatusDTO() {}

    public UpdateReservationStatusDTO(ReservationStatus status) {
        this.status = status;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
