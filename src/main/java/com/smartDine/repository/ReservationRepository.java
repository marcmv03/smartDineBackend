package com.smartDine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByTableIdAndTimeSlotId(Long tableId, Long timeSlotId);

    List<Reservation> findByCustomerId(Long customerId);
}
