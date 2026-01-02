package com.smartDine.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.ReservationDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.ReservationParticipation;
import com.smartDine.entity.ReservationStatus;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.Role;
import com.smartDine.entity.TimeSlot;
import com.smartDine.entity.User;
import com.smartDine.exceptions.ExpiredOpenReservationException;
import com.smartDine.exceptions.IllegalReservationStateChangeException;
import com.smartDine.repository.ReservationRepository;
import com.smartDine.repository.TimeSlotRepository;


@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private  RestaurantService restaurantService;
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    @Autowired
    private  RestaurantTableService restaurantTableService;
    @Autowired
    private  ReservationParticipationService reservationParticipationService;

    @Transactional
    public Reservation createReservation(ReservationDTO reservationDTO, Customer customer) {

        Restaurant restaurant = restaurantService.getRestaurantById(reservationDTO.getRestaurantId());
        TimeSlot timeSlot = timeSlotRepository.findById(reservationDTO.getTimeSlotId())
            .orElseThrow(() -> new IllegalArgumentException("Time slot not found with id: " + reservationDTO.getTimeSlotId()));

        if (!timeSlot.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Time slot does not belong to the provided restaurant");
        }

       RestaurantTable availableTable  = restaurantTableService.getTableById(reservationDTO.getTableId());
    

        Reservation reservation = ReservationDTO.toEntity(reservationDTO);
        reservation.setCustomer(customer);
        reservation.setRestaurant(restaurant);
        reservation.setTimeSlot(timeSlot);
        reservation.setRestaurantTable(availableTable);
        reservation.setDate(reservationDTO.getDate());
        reservation.setNumGuests(reservationDTO.getNumCustomers());
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setCreatedAt(LocalDate.now());
        
        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsForCustomer(Long customerId) {
        return reservationRepository.findByCustomerId(customerId);
    }
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByRestaurantAndDateAndTimeSlot(Long restaurantId, java.time.LocalDate date, Long timeSlotId) {
        return reservationRepository.findByRestaurantIdAndDateAndTimeSlotId(restaurantId, date, timeSlotId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByRestaurantAndDate(Long restaurantId, LocalDate date, Business business) {
        if (!restaurantService.isOwnerOfRestaurant(restaurantId, business)) {
            throw new IllegalArgumentException("You are not the owner of this restaurant");
        }
        return reservationRepository.findByRestaurantIdAndDate(restaurantId, date);
    }

    /**
     * Changes the status of a reservation.
     * 
     * Business rules:
     * - Only the customer who created the reservation OR the business owner of the restaurant can change the status.
     * - Customer can only change from CONFIRMED to CANCELLED.
     * - Business owner can change from CONFIRMED to CANCELLED or COMPLETED.
     * 
     * @param reservationId The ID of the reservation to update
     * @param newStatus The new status to set
     * @param user The user attempting to change the status
     * @return The updated reservation
     * @throws IllegalArgumentException if reservation not found
     * @throws BadCredentialsException if user is not authorized
     * @throws IllegalReservationStateChangeException if transition is not allowed
     */
    @Transactional
    public Reservation changeReservationStatus(Long reservationId, ReservationStatus newStatus, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        // Check authorization: user must be the customer who created it OR the business owner
        boolean isCustomerCreator = user.getRole() == Role.ROLE_CUSTOMER 
            && reservation.getCustomer().getId().equals(user.getId());
        boolean isBusinessOwner = user.getRole() == Role.ROLE_BUSINESS 
            && restaurantService.isOwnerOfRestaurant(reservation.getRestaurant().getId(), (Business) user);

        if (!isCustomerCreator && !isBusinessOwner) {
            throw new BadCredentialsException("You are not authorized to change this reservation's status");
        }

        // Validate current status is CONFIRMED
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalReservationStateChangeException(
                "Cannot change status: reservation is already " + reservation.getStatus()
            );
        }

        // Validate transition based on role
        if (newStatus == ReservationStatus.CANCELLED) {
            // Both customer and business can cancel
            reservation.setStatus(ReservationStatus.CANCELLED);
        } else if (newStatus == ReservationStatus.COMPLETED) {
            // Only business can complete
            if (!isBusinessOwner) {
                throw new IllegalReservationStateChangeException(
                    "Only the restaurant owner can mark a reservation as completed"
                );
            }
            reservation.setStatus(ReservationStatus.COMPLETED);
        } else {
            throw new IllegalReservationStateChangeException(
                "Invalid status transition from CONFIRMED to " + newStatus
            );
        }

        return reservationRepository.save(reservation);
    }

    /**
     * Retrieves a reservation by its ID.
     * 
     * @param reservationId The ID of the reservation
     * @return The reservation
     * @throws IllegalArgumentException if not found
     */
    @Transactional(readOnly = true)
    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));
    }

    /**
     * Checks if a customer is a participant in a reservation (either creator or joined participant).
     * 
     * @param reservation The reservation to check
     * @param customer The customer to verify
     * @return true if the customer is the creator or a participant
     */
    public boolean isParticipant(Reservation reservation, Customer customer) {
        // Check if customer is the creator
        if (reservation.getCustomer().getId().equals(customer.getId())) {
            return true;
        }
        // Check if customer is in participants set
        return reservation.getParticipants().stream()
            .anyMatch(p -> p.getId().equals(customer.getId()));
    }

    /**
     * Gets the total number of people in a reservation (creator + participants).
     * 
     * @param reservation The reservation
     * @return Total count of people
     */
    public int getTotalParticipantsCount(Reservation reservation) {
        return 1 + reservation.getParticipants().size();
    }

    /**
     * Checks if a customer has a conflicting reservation at the same time slot and date.
     * 
     * @param customer The customer to check
     * @param timeSlot The time slot
     * @param date The date
     * @param excludeReservationId Optional reservation ID to exclude from the check
     * @return true if there's a conflict
     */
    @Transactional(readOnly = true)
    public boolean hasTimeConflict(Customer customer, TimeSlot timeSlot, LocalDate date, Long excludeReservationId) {
        // Get all reservations for this customer on this date
        List<Reservation> customerReservations = reservationRepository.findByCustomerId(customer.getId());
        
        for (Reservation existing : customerReservations) {
            // Skip the reservation we're trying to join
            if (excludeReservationId != null && existing.getId().equals(excludeReservationId)) {
                continue;
            }
            
            // Only check confirmed reservations on the same date
            if (existing.getStatus() == ReservationStatus.CONFIRMED && existing.getDate().equals(date)) {
                // Check if time slots overlap
                TimeSlot existingSlot = existing.getTimeSlot();
                if (timeSlotsOverlap(existingSlot, timeSlot)) {
                    return true;
                }
            }
        }
        
        // Also check if the customer is a participant in other reservations
        // This requires checking all reservations where this customer is a participant
        List<Reservation> allReservations = reservationRepository.findAll();
        for (Reservation existing : allReservations) {
            if (excludeReservationId != null && existing.getId().equals(excludeReservationId)) {
                continue;
            }
            
            if (existing.getStatus() == ReservationStatus.CONFIRMED 
                && existing.getDate().equals(date)
                && existing.getParticipants().stream().anyMatch(p -> p.getId().equals(customer.getId()))) {
                TimeSlot existingSlot = existing.getTimeSlot();
                if (timeSlotsOverlap(existingSlot, timeSlot)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Checks if two time slots overlap.
     */
    private boolean timeSlotsOverlap(TimeSlot slot1, TimeSlot slot2) {
        // Time slots overlap if they're on the same day and times intersect
        if (slot1.getDayOfWeek() != slot2.getDayOfWeek()) {
            return false;
        }
        
        // Check if time ranges overlap
        // Overlap occurs when: start1 < end2 AND start2 < end1
        return slot1.getStartTime() < slot2.getEndTime() 
            && slot2.getStartTime() < slot1.getEndTime();
    }

    /**
     * Adds a participant to an existing reservation.
     * 
     * Validations:
     * - Reservation must exist
     * - Reservation date must not be in the past
     * - Reservation must be confirmed
     * - Customer must not already be a participant
     * - Adding participant must not exceed table capacity
     * - Customer must not have conflicting reservations
     * 
     * @param reservationId The reservation ID
     * @param customer The customer to add
     * @param maxAllowedParticipants The maximum participants allowed (from OpenReservationPost)
     * @throws IllegalArgumentException if reservation not found
     * @throws ExpiredOpenReservationException if reservation date is in the past
     * @throws IllegalReservationStateChangeException for other validation failures
     */
    @Transactional
    public void addParticipantToReservation(Long reservationId, Customer customer, int maxAllowedParticipants) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        // Check if reservation date has passed
        if (reservation.getDate().isBefore(LocalDate.now())) {
            throw new ExpiredOpenReservationException("Cannot join reservation: the reservation date has already passed");
        }

        // Check if reservation is confirmed
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalReservationStateChangeException(
                "Cannot join reservation: reservation is " + reservation.getStatus()
            );
        }

        // Check if customer is already a participant
        if (isParticipant(reservation, customer)) {
            throw new IllegalReservationStateChangeException("You are already a participant in this reservation");
        }

        // Check capacity: current participants (excluding creator) must be less than maxAllowedParticipants
        if (reservation.getParticipants().size() >= maxAllowedParticipants) {
            throw new IllegalReservationStateChangeException("No available slots: reservation is full");
        }

        // Check table capacity
        int currentTotal = getTotalParticipantsCount(reservation);
        if (currentTotal >= reservation.getRestaurantTable().getCapacity()) {
            throw new IllegalReservationStateChangeException(
                "Cannot join: adding you would exceed the table capacity of " + reservation.getRestaurantTable().getCapacity()
            );
        }

        // Check for time conflicts
        if (hasTimeConflict(customer, reservation.getTimeSlot(), reservation.getDate(), reservationId)) {
            throw new IllegalReservationStateChangeException(
                "Cannot join: you have a conflicting reservation at the same time"
            );
        }

        // All validations passed - add the participant
        reservation.getParticipants().add(customer);
        reservationRepository.save(reservation);
    }
    public List<Reservation> getAllJoinedReservationsByCustomer(Long customerId) {
        List<ReservationParticipation> participations = reservationParticipationService.getUserParticipations(customerId) ;
        return participations.stream()
            .map(ReservationParticipation::getReservation)
            .toList();
}
}
