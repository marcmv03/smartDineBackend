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
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reservation r " +
           "WHERE r.restaurantTable.id = :tableId AND r.timeSlot.id = :timeSlotId")
    boolean existsByRestaurantTableIdAndTimeSlotId(
        @Param("tableId") Long tableId, 
        @Param("timeSlotId") Long timeSlotId
    );
}