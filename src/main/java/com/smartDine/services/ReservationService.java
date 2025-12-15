package com.smartDine.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.ReservationDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.ReservationStatus;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.TimeSlot;
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
}
