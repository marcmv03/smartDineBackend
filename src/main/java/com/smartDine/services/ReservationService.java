package com.smartDine.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.ReservationDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.ReservationStatus;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.Role;
import com.smartDine.entity.TimeSlot;
import com.smartDine.entity.User;
import com.smartDine.exceptions.IllegalReservationStateChangeException;
import com.smartDine.repository.ReservationRepository;
import com.smartDine.repository.TimeSlotRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantService restaurantService;
    private final TimeSlotRepository timeSlotRepository;
    private final RestaurantTableService restaurantTableService;

    public ReservationService(
        ReservationRepository reservationRepository,
        RestaurantService restaurantService,
        TimeSlotRepository timeSlotRepository,
        RestaurantTableService restaurantTableService
    ) {
        this.reservationRepository = reservationRepository;
        this.restaurantService = restaurantService;
        this.timeSlotRepository = timeSlotRepository;
        this.restaurantTableService = restaurantTableService;
    }

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
}
