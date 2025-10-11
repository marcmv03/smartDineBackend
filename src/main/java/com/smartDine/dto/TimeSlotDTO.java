package com.smartDine.dto;

import java.time.DayOfWeek;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class TimeSlotDTO {
    @NotNull(message = "Start time is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Start time must be greater than or equal to 0")
    @DecimalMax(value = "24.0", inclusive = true, message = "Start time must be less than or equal to 24")
    private Double startTime;

    @NotNull(message = "End time is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "End time must be greater than or equal to 0")
    @DecimalMax(value = "24.0", inclusive = true, message = "End time must be less than or equal to 24")
    private Double endTime;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Restaurant is required")
    private Long restaurantId;

    @AssertTrue(message = "Start time must be earlier than end time")
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return startTime < endTime;
    }

    public Double getStartTime() {
        return startTime;
    }

    public void setStartTime(Double startTime) {
        this.startTime = startTime;
    }

    public Double getEndTime() {
        return endTime;
    }

    public void setEndTime(Double endTime) {
        this.endTime = endTime;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }
}
