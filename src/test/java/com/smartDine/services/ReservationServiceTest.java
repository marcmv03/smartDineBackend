package com.smartDine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.smartDine.dto.ReservationDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.Table;
import com.smartDine.entity.TimeSlot;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.RestaurantRepository;
import com.smartDine.repository.TableRepository;
import com.smartDine.repository.TimeSlotRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Should create a reservation and assign an available table")
    void createReservationAssignsTable() {
        Business owner = createBusiness("Owner", "owner@smartdine.com", 111111111L);
        Restaurant restaurant = createRestaurant(owner, "Owner Restaurant");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.MONDAY, 12.0, 14.0);
        Table table = createTable(restaurant, 1, 4);
        Customer customer = createCustomer("Alice", "alice@smartdine.com", 222222222L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setNumCustomers(2);

        Reservation reservation = reservationService.createReservation(dto, customer);

        assertNotNull(reservation.getId());
        assertEquals(timeSlot.getId(), reservation.getTimeSlot().getId());
        assertEquals(table.getId(), reservation.getTable().getId());
        assertEquals(2, reservation.getNumberOfGuests());
    }

    @Test
    @DisplayName("Should fail when no tables are available for the time slot")
    void createReservationWithoutAvailableTable() {
        Business owner = createBusiness("Owner2", "owner2@smartdine.com", 333333333L);
        Restaurant restaurant = createRestaurant(owner, "Restaurant Two");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.TUESDAY, 18.0, 20.0);
        createTable(restaurant, 5, 4);
        Customer customer = createCustomer("Bob", "bob@smartdine.com", 444444444L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setNumCustomers(4);

        reservationService.createReservation(dto, customer);

        ReservationDTO secondAttempt = new ReservationDTO();
        secondAttempt.setRestaurantId(restaurant.getId());
        secondAttempt.setTimeSlotId(timeSlot.getId());
        secondAttempt.setNumCustomers(2);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationService.createReservation(secondAttempt, customer)
        );

        assertEquals("No tables available for the selected time slot", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when the time slot does not belong to the restaurant")
    void createReservationWithMismatchedTimeSlot() {
        Business owner = createBusiness("Owner3", "owner3@smartdine.com", 555555555L);
        Restaurant restaurantOne = createRestaurant(owner, "Restaurant One");
        Restaurant restaurantTwo = createRestaurant(owner, "Restaurant Two");
        TimeSlot otherTimeSlot = createTimeSlot(restaurantTwo, DayOfWeek.WEDNESDAY, 10.0, 12.0);
        createTable(restaurantOne, 10, 4);
        Customer customer = createCustomer("Carol", "carol@smartdine.com", 666666666L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurantOne.getId());
        dto.setTimeSlotId(otherTimeSlot.getId());
        dto.setNumCustomers(2);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationService.createReservation(dto, customer)
        );

        assertEquals("Time slot does not belong to the provided restaurant", exception.getMessage());
    }

    private Business createBusiness(String name, String email, Long phoneNumber) {
        Business business = new Business(name, email, "password", phoneNumber);
        business.setRestaurants(new ArrayList<>());
        return businessRepository.save(business);
    }

    private Restaurant createRestaurant(Business owner, String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress("123 Main St");
        restaurant.setDescription("Description");
        restaurant.setOwner(owner);
        Restaurant saved = restaurantRepository.save(restaurant);
        owner.getRestaurants().add(saved);
        businessRepository.save(owner);
        return saved;
    }

    private TimeSlot createTimeSlot(Restaurant restaurant, DayOfWeek dayOfWeek, double start, double end) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setRestaurant(restaurant);
        timeSlot.setDayOfWeek(dayOfWeek);
        timeSlot.setStartTime(start);
        timeSlot.setEndTime(end);
        return timeSlotRepository.save(timeSlot);
    }

    private Table createTable(Restaurant restaurant, int number, int capacity) {
        Table table = new Table();
        table.setRestaurant(restaurant);
        table.setNumber(number);
        table.setCapacity(capacity);
        table.setOutside(false);
        return tableRepository.save(table);
    }

    private Customer createCustomer(String name, String email, Long phoneNumber) {
        Customer customer = new Customer(name, email, "password", phoneNumber);
        customer.setReservations(new ArrayList<>());
        return customerRepository.save(customer);
    }
}
