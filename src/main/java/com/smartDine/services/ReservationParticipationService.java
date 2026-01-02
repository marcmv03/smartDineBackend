package com.smartDine.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.ReservationParticipation;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.ReservationParticipationRepository;
import com.smartDine.repository.ReservationRepository;

@Service
public class ReservationParticipationService {

    private final ReservationParticipationRepository participationRepository;
    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;

    public ReservationParticipationService(
            ReservationParticipationRepository participationRepository,
            ReservationRepository reservationRepository,
            CustomerRepository customerRepository) {
        this.participationRepository = participationRepository;
        this.reservationRepository = reservationRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Creates a new participation record for a user in a reservation.
     * 
     * @param userId The ID of the customer joining the reservation
     * @param reservationId The ID of the reservation to join
     * @return The created ReservationParticipation
     * @throws IllegalArgumentException if user or reservation not found, or user already participates
     */
    @Transactional
    public ReservationParticipation createNewParticipation(Long userId, Long reservationId) {
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + userId));
        
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        // Check if user already participates
        if (participationRepository.existsByReservationAndCustomer(reservation, customer)) {
            throw new IllegalArgumentException("User already participates in this reservation");
        }

        ReservationParticipation participation = new ReservationParticipation(reservation, customer);
        return participationRepository.save(participation);
    }

    /**
     * Gets all participations for a specific user.
     * 
     * @param userId The ID of the customer
     * @return List of ReservationParticipation for the user
     * @throws IllegalArgumentException if user not found
     */
    @Transactional(readOnly = true)
    public List<ReservationParticipation> getUserParticipations(Long userId) {
        if (customerRepository.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("Customer not found with id: " + userId);
        }
        return participationRepository.findByCustomerId(userId);
    }

    /**
     * Gets all participants for a specific reservation.
     * 
     * @param reservationId The ID of the reservation
     * @return List of ReservationParticipation for the reservation
     * @throws IllegalArgumentException if reservation not found
     */
    @Transactional(readOnly = true)
    public List<ReservationParticipation> getParticipants(Long reservationId) {
        if (!reservationRepository.existsById(reservationId)) {
            throw new IllegalArgumentException("Reservation not found with id: " + reservationId);
        }
        return participationRepository.findByReservationId(reservationId);
    }

    /**
     * Checks if a user is already a participant of a reservation.
     * 
     * @param userId The ID of the customer
     * @param reservationId The ID of the reservation
     * @return true if the user is already a participant
     */
    @Transactional(readOnly = true)
    public boolean isParticipant(Long userId, Long reservationId) {
        return participationRepository.existsByReservationIdAndCustomerId(reservationId, userId);
    }
}
