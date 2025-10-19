package com.smartDine.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.ReservationDTO;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.Table;
import com.smartDine.entity.TimeSlot;
import com.smartDine.repository.ReservationRepository;
import com.smartDine.repository.TableRepository;
import com.smartDine.repository.TimeSlotRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantService restaurantService;
    private final TimeSlotRepository timeSlotRepository;
    private final TableRepository tableRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        RestaurantService restaurantService,
        TimeSlotRepository timeSlotRepository,
        TableRepository tableRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.restaurantService = restaurantService;
        this.timeSlotRepository = timeSlotRepository;
        this.tableRepository = tableRepository;
    }

    @Transactional
    public Reservation createReservation(ReservationDTO reservationDTO, Customer customer) {
        if (customer == null || customer.getId() == null) {
            throw new IllegalArgumentException("Customer is required to create a reservation");
        }
        if (reservationDTO.getNumCustomers() <= 0) {
            throw new IllegalArgumentException("Number of customers must be greater than zero");
        }

        Restaurant restaurant = restaurantService.getRestaurantById(reservationDTO.getRestaurantId());
        TimeSlot timeSlot = timeSlotRepository.findById(reservationDTO.getTimeSlotId())
            .orElseThrow(() -> new IllegalArgumentException("Time slot not found with id: " + reservationDTO.getTimeSlotId()));

        if (!timeSlot.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Time slot does not belong to the provided restaurant");
        }

        List<Table> candidateTables = tableRepository.findByRestaurantIdAndCapacityGreaterThanEqual(
            restaurant.getId(),
            reservationDTO.getNumCustomers()
        );

        Table availableTable = candidateTables.stream()
            .filter(table -> !reservationRepository.existsByTableIdAndTimeSlotId(table.getId(), timeSlot.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No tables available for the selected time slot"));

        Reservation reservation = ReservationDTO.toEntity(reservationDTO);
        reservation.setCustomer(customer);
        reservation.setRestaurant(restaurant);
        reservation.setTimeSlot(timeSlot);
        reservation.setTable(availableTable);

        Reservation savedReservation = reservationRepository.save(reservation);
        customer.getReservations().add(savedReservation);
        return savedReservation;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsForCustomer(Long customerId) {
        return reservationRepository.findByCustomerId(customerId);
    }
}
