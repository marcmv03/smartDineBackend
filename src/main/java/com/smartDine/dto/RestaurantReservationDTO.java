package com.smartDine.dto;

import java.util.List;

import com.smartDine.entity.Reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for returning reservation information to restaurant owners.
 * Contains reservation details with customer name and table information.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantReservationDTO {
    private Long id;
    private String username;
    private Double startTime;
    private Double endTime;
    private Integer numTable;
    private Boolean outside;

    public static RestaurantReservationDTO fromEntity(Reservation reservation) {
        return new RestaurantReservationDTO(
            reservation.getId(),
            reservation.getCustomer().getName(),
            reservation.getTimeSlot().getStartTime(),
            reservation.getTimeSlot().getEndTime(),
            reservation.getRestaurantTable().getNumber(),
            reservation.getRestaurantTable().getOutside()
        );
    }

    public static List<RestaurantReservationDTO> fromEntity(List<Reservation> reservations) {
        return reservations.stream()
            .map(RestaurantReservationDTO::fromEntity)
            .toList();
    }
}
