package com.smartDine.dto;

import java.util.List;

import com.smartDine.entity.Reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ReservationDetailsDTO {
    private Long reservationId;
    private String restaurantName;
    private String imageKey ;
    private String reservationDate;
    private Double startTime;
    private Double endTime;
    private int numberOfGuests;
    private String address ;
public static ReservationDetailsDTO fromEntity(Reservation reservation) {
    return new ReservationDetailsDTO(
        reservation.getId(),
        reservation.getRestaurant().getName(),
        reservation.getRestaurant().getImageUrl(),
        reservation.getDate().toString(),
        reservation.getTimeSlot().getStartTime(),
        reservation.getTimeSlot().getEndTime(),
        reservation.getNumGuests(),
        reservation.getRestaurant().getAddress()
    );
}
public static List<ReservationDetailsDTO> fromEntity(List<Reservation> reservations) {
    return reservations.stream()
        .map(ReservationDetailsDTO::fromEntity)
        .toList();
}
}