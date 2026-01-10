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
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private FriendshipService friendshipService;
    @Autowired
    private CustomerService customerService;

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
        
        Reservation savedReservation = reservationRepository.save(reservation);
        
        // Notify restaurant owner about the new reservation
        String message = String.format("%s ha hecho una reserva en el %s a las %.0f el día %s",
                customer.getName(),
                restaurant.getName(),
                timeSlot.getStartTime(),
                reservationDTO.getDate().toString());
        notificationService.createNotification(restaurant.getOwner(), message);
        
        return savedReservation;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsForCustomer(Long customerId) {
        return reservationRepository.findByCustomerId(customerId);
    }

    /**
     * Gets all reservations for a customer, including both owned and participated reservations.
     * 
     * @param customerId The ID of the customer
     * @return List of all reservations (owned + participated) with duplicates removed
     */
    @Transactional(readOnly = true)
    public List<Reservation> getAllReservationsForCustomer(Long customerId) {
        // Get reservations owned by the customer
        List<Reservation> ownedReservations = reservationRepository.findByCustomerId(customerId);
        
        // Get reservations the customer participates in
        List<ReservationParticipation> participations = reservationParticipationService.getUserParticipations(customerId);
        List<Reservation> participatedReservations = participations.stream()
                .map(ReservationParticipation::getReservation)
                .toList();
        
        // Combine both lists and remove duplicates
        return java.util.stream.Stream.concat(
                ownedReservations.stream(),
                participatedReservations.stream()
        ).distinct().toList();
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
     * Checks if a customer is a participant of a reservation (either as creator or participant).
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
        // Check if customer is in participation records
        return reservationParticipationService.isParticipant(customer.getId(), reservation.getId());
    }

    /**
     * Gets the total number of people in a reservation (creator + participants).
     * 
     * @param reservation The reservation
     * @return Total count of people
     */
    public int getTotalParticipantsCount(Reservation reservation) {
        int participantsCount = reservationParticipationService.getParticipants(reservation.getId()).size();
        return 1 + participantsCount;  // 1 for creator + participants
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
        List<ReservationParticipation> participations = reservationParticipationService.getUserParticipations(customer.getId());
        for (ReservationParticipation participation : participations) {
            Reservation existing = participation.getReservation();
            if (excludeReservationId != null && existing.getId().equals(excludeReservationId)) {
                continue;
            }
            
            if (existing.getStatus() == ReservationStatus.CONFIRMED 
                && existing.getDate().equals(date)) {
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
        int currentParticipantsCount = reservationParticipationService.getParticipants(reservationId).size();
        if (currentParticipantsCount >= maxAllowedParticipants) {
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

        // All validations passed - add the participant using ReservationParticipation
        reservationParticipationService.createNewParticipation(customer.getId(), reservationId);
    }
    public List<Reservation> getAllJoinedReservationsByCustomer(Long customerId) {
        List<ReservationParticipation> participations = reservationParticipationService.getUserParticipations(customerId) ;
        return participations.stream()
            .map(ReservationParticipation::getReservation)
            .toList();
    }

    /**
     * Gets all participants (customers) of a specific reservation.
     * Only the reservation owner or participants can access this information.
     * 
     * @param reservationId The ID of the reservation
     * @param requestingUserId The ID of the user making the request
     * @return List of Customer entities who are participants (excludes the owner)
     * @throws IllegalArgumentException if reservation not found
     * @throws BadCredentialsException if requesting user is not owner or participant
     */
    @Transactional(readOnly = true)
    public List<Customer> getReservationParticipants(Long reservationId, Long requestingUserId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        // Check if requesting user is the owner
        boolean isOwner = reservation.getCustomer().getId().equals(requestingUserId);
        
        // Check if requesting user is a participant
        boolean isParticipant = reservationParticipationService.isParticipant(requestingUserId, reservationId);

        if (!isOwner && !isParticipant) {
            throw new BadCredentialsException("You are not authorized to view participants of this reservation");
        }

        return reservationParticipationService.getParticipantCustomers(reservationId);
    }

    /**
     * Adds a friend as a participant to a reservation.
     * 
     * Validations:
     * - Requester must be the owner of the reservation
     * - Friend must exist and be a Customer
     * - Requester and friend must be friends
     * - Friend must not already be a participant
     * - Adding friend must not exceed table capacity
     * - Friend must not have conflicting reservations
     * 
     * @param reservationId The ID of the reservation
     * @param friendId The ID of the friend to add
     * @param ownerId The ID of the reservation owner making the request
     * @return The created ReservationParticipation
     * @throws IllegalArgumentException if reservation or friend not found
     * @throws BadCredentialsException if requester is not the owner
     * @throws IllegalReservationStateChangeException for other validation failures
     */
    @Transactional
    public ReservationParticipation addFriendAsParticipant(Long reservationId, Long friendId, Long ownerId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        // Check if requester is the owner
        if (!reservation.getCustomer().getId().equals(ownerId)) {
            throw new BadCredentialsException("Only the reservation owner can add participants");
        }

        // Get the friend (must be a Customer)
        Customer friend = customerService.getCustomerById(friendId);

        // Check if they are actually friends
        Customer owner = reservation.getCustomer();
        if (!friendshipService.areFriends(owner, friend)) {
            throw new IllegalArgumentException("You can only add friends as participants");
        }

        // Check if reservation date has passed
        if (reservation.getDate().isBefore(LocalDate.now())) {
            throw new ExpiredOpenReservationException("Cannot add participant: the reservation date has already passed");
        }

        // Check if reservation is confirmed
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalReservationStateChangeException(
                "Cannot add participant: reservation is " + reservation.getStatus()
            );
        }

        // Check if friend is already a participant
        if (isParticipant(reservation, friend)) {
            throw new IllegalReservationStateChangeException("This user is already a participant in the reservation");
        }

        // Check table capacity
        int currentTotal = getTotalParticipantsCount(reservation);
        if (currentTotal >= reservation.getRestaurantTable().getCapacity()) {
            throw new IllegalReservationStateChangeException(
                "Cannot add participant: would exceed table capacity of " + reservation.getRestaurantTable().getCapacity()
            );
        }

        // Check for time conflicts
        if (hasTimeConflict(friend, reservation.getTimeSlot(), reservation.getDate(), reservationId)) {
            throw new IllegalReservationStateChangeException(
                "Cannot add participant: they have a conflicting reservation at the same time"
            );
        }

        // All validations passed - add the participant
        ReservationParticipation participation = reservationParticipationService
                .createNewParticipation(friendId, reservationId);

        // Notify the friend that they've been added
        String message = String.format("Has sido añadido a la reserva de %s en %s el día %s",
                owner.getName(),
                reservation.getRestaurant().getName(),
                reservation.getDate().toString());
        notificationService.createNotification(friend, message);

        return participation;
    }

    /**
     * Removes a participant from a reservation.
     * 
     * Validations:
     * - Requester must be the owner OR the participant being removed (self-removal)
     * - Cannot remove the reservation owner
     * 
     * @param reservationId The ID of the reservation
     * @param participantId The ID of the participant to remove
     * @param requesterId The ID of the user making the request
     * @throws IllegalArgumentException if reservation not found or participant not in reservation
     * @throws BadCredentialsException if requester is not authorized
     * @throws IllegalReservationStateChangeException if trying to remove the owner
     */
    @Transactional
    public void removeParticipantFromReservation(Long reservationId, Long participantId, Long requesterId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        // Cannot remove the reservation owner
        if (reservation.getCustomer().getId().equals(participantId)) {
            throw new IllegalReservationStateChangeException(
                "Cannot remove the reservation owner. Use cancel reservation instead."
            );
        }

        boolean isOwner = reservation.getCustomer().getId().equals(requesterId);
        boolean isSelfRemoval = participantId.equals(requesterId);

        // Only owner or the participant themselves can remove
        if (!isOwner && !isSelfRemoval) {
            throw new BadCredentialsException("Only the reservation owner or the participant themselves can remove a participant");
        }

        // Remove the participation
        reservationParticipationService.removeParticipation(reservationId, participantId);

        // Notify the participant if removed by owner (not self-removal)
        if (isOwner && !isSelfRemoval) {
            Customer participant = customerService.getCustomerById(participantId);
            String message = String.format("Has sido eliminado de la reserva en %s el día %s",
                    reservation.getRestaurant().getName(),
                    reservation.getDate().toString());
            notificationService.createNotification(participant, message);
        }
    }
}

