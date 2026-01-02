package com.smartDine.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.ReservationParticipation;

@Repository
public interface ReservationParticipationRepository extends JpaRepository<ReservationParticipation, Long> {
    
    /**
     * Find all participations for a specific customer.
     */
    List<ReservationParticipation> findByCustomer(Customer customer);
    
    /**
     * Find all participations for a specific customer by ID.
     */
    List<ReservationParticipation> findByCustomerId(Long customerId);
    
    /**
     * Find all participants for a specific reservation.
     */
    List<ReservationParticipation> findByReservation(Reservation reservation);
    
    /**
     * Find all participants for a specific reservation by ID.
     */
    List<ReservationParticipation> findByReservationId(Long reservationId);
    
    /**
     * Check if a customer already participates in a reservation.
     */
    boolean existsByReservationAndCustomer(Reservation reservation, Customer customer);
    
    /**
     * Check if a customer already participates in a reservation by IDs.
     */
    boolean existsByReservationIdAndCustomerId(Long reservationId, Long customerId);
    
    /**
     * Find a specific participation by reservation and customer.
     */
    Optional<ReservationParticipation> findByReservationAndCustomer(Reservation reservation, Customer customer);
    
    /**
     * Find a specific participation by reservation ID and customer ID.
     */
    Optional<ReservationParticipation> findByReservationIdAndCustomerId(Long reservationId, Long customerId);
}
