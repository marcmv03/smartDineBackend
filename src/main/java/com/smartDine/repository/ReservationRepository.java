package com.smartDine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByCustomerId(Long customerId);
    
    // Usar el nombre correcto de la columna: table_id
    @Query("SELECT r FROM Reservation r WHERE r.restaurant.id = :restaurantId AND r.date = :date AND r.timeSlot.id = :timeSlotId")
    List<Reservation> findByRestaurantIdAndDateAndTimeSlotId(
        @Param("restaurantId") Long restaurantId,
        @Param("date") java.time.LocalDate date,
        @Param("timeSlotId") Long timeSlotId
    );

    List<Reservation> findByRestaurantIdAndDate(Long restaurantId, java.time.LocalDate date);
}