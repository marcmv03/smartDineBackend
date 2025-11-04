package com.smartDine.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.Reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReservationDTO {
    private Long id;

    @NotNull(message = "Time slot is required")
    private Long timeSlotId;

    @NotNull(message = "Restaurant is required")
    private Long restaurantId;
    @NotNull(message = "Table is required")
    @Min(value = 1, message = "Table ID must be positive")
    private Long tableId;
    private Long customerId;

    @NotNull(message = "Number of customers is required")
    @Min(value = 1, message = "Number of customers must be at least 1")
    private int numCustomers;

    @NotNull(message = "Reservation date is required")
    private LocalDate date;

    public ReservationDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Long timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public int getNumCustomers() {
        return numCustomers;
    }

    public void setNumCustomers(int numCustomers) {
        this.numCustomers = numCustomers;
    }

    public static Reservation toEntity(ReservationDTO dto) {
        Reservation reservation = new Reservation();
        if (dto.getId() != null) {
            reservation.setId(dto.getId());
        }
        reservation.setNumGuests(dto.getNumCustomers());
        return reservation;
    }

    public static ReservationDTO fromEntity(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setNumCustomers(reservation.getNumGuests());
        if (reservation.getTimeSlot() != null) {
            dto.setTimeSlotId(reservation.getTimeSlot().getId());
        }
        if (reservation.getRestaurant() != null) {
            dto.setRestaurantId(reservation.getRestaurant().getId());
        }
        if (reservation.getRestaurantTable() != null) {
            dto.setTableId(reservation.getRestaurantTable().getId());
        }
        if (reservation.getCustomer() != null) {
            dto.setCustomerId(reservation.getCustomer().getId());
        }
        if(reservation.getDate() != null) {
            dto.setDate(reservation.getDate());
        }
        return dto;
    }

    public static List<ReservationDTO> fromEntity(List<Reservation> reservations) {
        return reservations.stream()
            .map(ReservationDTO::fromEntity)
            .collect(Collectors.toList());
    }

    private void setDate(LocalDate date) {
        this.date = date;
    }
}
