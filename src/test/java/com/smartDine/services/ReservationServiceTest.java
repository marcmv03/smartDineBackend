package com.smartDine.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.TimeSlot;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.RestaurantRepository;
import com.smartDine.repository.RestaurantTableRepository;
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
    private RestaurantTableRepository tableRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Should create a reservation with specified table and date")
    void createReservationAssignsTable() {
        Business owner = createBusiness("Owner", "owner@smartdine.com", 111111111L);
        Restaurant restaurant = createRestaurant(owner, "Owner Restaurant");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.MONDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 1, 4);
        Customer customer = createCustomer("Alice", "alice@smartdine.com", 222222222L);

        LocalDate reservationDate = LocalDate.now().plusDays(1);
        
        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(reservationDate);

        Reservation reservation = reservationService.createReservation(dto, customer);
        
        assertNotNull(reservation.getId());
        assertEquals(timeSlot.getId(), reservation.getTimeSlot().getId());
        assertEquals(table.getId(), reservation.getRestaurantTable().getId());
        assertEquals(2, reservation.getNumGuests());
        assertEquals(customer.getId(), reservation.getCustomer().getId());
        assertEquals(reservationDate, reservation.getDate());
        assertEquals(LocalDate.now(), reservation.getCreatedAt());
    }

    @Test
    @DisplayName("Should fail when the time slot does not belong to the restaurant")
    void createReservationWithMismatchedTimeSlot() {
        Business owner = createBusiness("Owner3", "owner3@smartdine.com", 555555555L);
        Restaurant restaurantOne = createRestaurant(owner, "Restaurant One");
        Restaurant restaurantTwo = createRestaurant(owner, "Restaurant Two");
        TimeSlot otherTimeSlot = createTimeSlot(restaurantTwo, DayOfWeek.WEDNESDAY, 10.0, 12.0);
        RestaurantTable table = createTable(restaurantOne, 10, 4);
        Customer customer = createCustomer("Carol", "carol@smartdine.com", 666666666L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurantOne.getId());
        dto.setTimeSlotId(otherTimeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(1));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationService.createReservation(dto, customer)
        );

        assertEquals("Time slot does not belong to the provided restaurant", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when table not found")
    void createReservationWithInvalidTable() {
        Business owner = createBusiness("Owner4", "owner4@smartdine.com", 777777777L);
        Restaurant restaurant = createRestaurant(owner, "Test Restaurant");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.FRIDAY, 18.0, 20.0);
        Customer customer = createCustomer("Dave", "dave@smartdine.com", 888888888L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(999L); // Non-existent table
        dto.setNumCustomers(4);
        dto.setDate(LocalDate.now().plusDays(2));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationService.createReservation(dto, customer)
        );

        assertEquals("Table not found with id: 999", exception.getMessage());
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

    private RestaurantTable createTable(Restaurant restaurant, int number, int capacity) {
        RestaurantTable table = new RestaurantTable();
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
