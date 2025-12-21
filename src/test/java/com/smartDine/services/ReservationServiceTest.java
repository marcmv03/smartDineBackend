package com.smartDine.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;

import com.smartDine.dto.ReservationDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.ReservationStatus;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.TimeSlot;
import com.smartDine.exceptions.IllegalReservationStateChangeException;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.RestaurantRepository;
import com.smartDine.repository.RestaurantTableRepository;
import com.smartDine.repository.TimeSlotRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
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

    @Test
    @DisplayName("Should get reservations for restaurant by date as owner")
    void getReservationsByRestaurantAndDateAsOwner() {
        Business owner = createBusiness("Owner5", "owner5@smartdine.com", 999999999L);
        Restaurant restaurant = createRestaurant(owner, "Owner Restaurant 5");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.TUESDAY, 18.0, 20.0);
        RestaurantTable table = createTable(restaurant, 5, 4);
        Customer customer = createCustomer("Eve", "eve@smartdine.com", 111222333L);

        LocalDate reservationDate = LocalDate.now().plusDays(3);
        
        // Create a reservation
        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(3);
        dto.setDate(reservationDate);
        reservationService.createReservation(dto, customer);

        // Get reservations as owner
        List<Reservation> reservations = reservationService.getReservationsByRestaurantAndDate(
            restaurant.getId(), reservationDate, owner);
        
        assertNotNull(reservations);
        assertEquals(1, reservations.size());
        assertEquals(customer.getName(), reservations.get(0).getCustomer().getName());
        assertEquals(table.getNumber(), reservations.get(0).getRestaurantTable().getNumber());
    }

    @Test
    @DisplayName("Should fail to get reservations when not owner")
    void getReservationsByRestaurantAndDateAsNonOwner() {
        Business owner = createBusiness("Owner6", "owner6@smartdine.com", 888777666L);
        Business nonOwner = createBusiness("NonOwner", "nonowner@smartdine.com", 555444333L);
        Restaurant restaurant = createRestaurant(owner, "Owner Restaurant 6");

        LocalDate reservationDate = LocalDate.now().plusDays(4);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationService.getReservationsByRestaurantAndDate(restaurant.getId(), reservationDate, nonOwner)
        );

        assertEquals("You are not the owner of this restaurant", exception.getMessage());
    }

    // ==================== changeReservationStatus Tests ====================

    @Test
    @DisplayName("Should allow customer to cancel their own reservation")
    void changeReservationStatus_CustomerCancelsOwnReservation_Success() {
        Business owner = createBusiness("OwnerCancel1", "ownercancel1@smartdine.com", 100000001L);
        Restaurant restaurant = createRestaurant(owner, "Cancel Restaurant 1");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.MONDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 1, 4);
        Customer customer = createCustomer("CancelCustomer1", "cancelcustomer1@smartdine.com", 200000001L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(1));

        Reservation reservation = reservationService.createReservation(dto, customer);
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());

        Reservation updated = reservationService.changeReservationStatus(
            reservation.getId(), ReservationStatus.CANCELLED, customer
        );

        assertEquals(ReservationStatus.CANCELLED, updated.getStatus());
    }

    @Test
    @DisplayName("Should allow business owner to cancel a reservation")
    void changeReservationStatus_BusinessCancelsReservation_Success() {
        Business owner = createBusiness("OwnerCancel2", "ownercancel2@smartdine.com", 100000002L);
        Restaurant restaurant = createRestaurant(owner, "Cancel Restaurant 2");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.TUESDAY, 18.0, 20.0);
        RestaurantTable table = createTable(restaurant, 2, 4);
        Customer customer = createCustomer("CancelCustomer2", "cancelcustomer2@smartdine.com", 200000002L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(3);
        dto.setDate(LocalDate.now().plusDays(2));

        Reservation reservation = reservationService.createReservation(dto, customer);

        Reservation updated = reservationService.changeReservationStatus(
            reservation.getId(), ReservationStatus.CANCELLED, owner
        );

        assertEquals(ReservationStatus.CANCELLED, updated.getStatus());
    }

    @Test
    @DisplayName("Should allow business owner to complete a reservation")
    void changeReservationStatus_BusinessCompletesReservation_Success() {
        Business owner = createBusiness("OwnerComplete", "ownercomplete@smartdine.com", 100000003L);
        Restaurant restaurant = createRestaurant(owner, "Complete Restaurant");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.WEDNESDAY, 19.0, 21.0);
        RestaurantTable table = createTable(restaurant, 3, 6);
        Customer customer = createCustomer("CompleteCustomer", "completecustomer@smartdine.com", 200000003L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(4);
        dto.setDate(LocalDate.now().plusDays(3));

        Reservation reservation = reservationService.createReservation(dto, customer);

        Reservation updated = reservationService.changeReservationStatus(
            reservation.getId(), ReservationStatus.COMPLETED, owner
        );

        assertEquals(ReservationStatus.COMPLETED, updated.getStatus());
    }

    @Test
    @DisplayName("Should reject customer trying to complete a reservation")
    void changeReservationStatus_CustomerTriesToComplete_ThrowsIllegalStateChange() {
        Business owner = createBusiness("OwnerReject1", "ownerreject1@smartdine.com", 100000004L);
        Restaurant restaurant = createRestaurant(owner, "Reject Restaurant 1");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.THURSDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 4, 2);
        Customer customer = createCustomer("RejectCustomer1", "rejectcustomer1@smartdine.com", 200000004L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(4));

        Reservation reservation = reservationService.createReservation(dto, customer);

        IllegalReservationStateChangeException exception = assertThrows(
            IllegalReservationStateChangeException.class,
            () -> reservationService.changeReservationStatus(
                reservation.getId(), ReservationStatus.COMPLETED, customer
            )
        );

        assertEquals("Only the restaurant owner can mark a reservation as completed", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject invalid status transition from CANCELLED")
    void changeReservationStatus_FromCancelled_ThrowsIllegalStateChange() {
        Business owner = createBusiness("OwnerInvalid1", "ownerinvalid1@smartdine.com", 100000005L);
        Restaurant restaurant = createRestaurant(owner, "Invalid Restaurant 1");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.FRIDAY, 20.0, 22.0);
        RestaurantTable table = createTable(restaurant, 5, 4);
        Customer customer = createCustomer("InvalidCustomer1", "invalidcustomer1@smartdine.com", 200000005L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(5));

        Reservation reservation = reservationService.createReservation(dto, customer);
        
        // First cancel the reservation
        reservationService.changeReservationStatus(reservation.getId(), ReservationStatus.CANCELLED, customer);

        // Then try to complete it (should fail)
        IllegalReservationStateChangeException exception = assertThrows(
            IllegalReservationStateChangeException.class,
            () -> reservationService.changeReservationStatus(
                reservation.getId(), ReservationStatus.COMPLETED, owner
            )
        );

        assertEquals("Cannot change status: reservation is already CANCELLED", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject unauthorized user trying to change status")
    void changeReservationStatus_UnauthorizedUser_ThrowsBadCredentials() {
        Business owner = createBusiness("OwnerAuth1", "ownerauth1@smartdine.com", 100000006L);
        Business otherBusiness = createBusiness("OtherBusiness", "otherbusiness@smartdine.com", 100000007L);
        Restaurant restaurant = createRestaurant(owner, "Auth Restaurant 1");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.SATURDAY, 13.0, 15.0);
        RestaurantTable table = createTable(restaurant, 6, 4);
        Customer customer = createCustomer("AuthCustomer1", "authcustomer1@smartdine.com", 200000006L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(6));

        Reservation reservation = reservationService.createReservation(dto, customer);

        // Another business (not the owner) tries to cancel
        BadCredentialsException exception = assertThrows(
            BadCredentialsException.class,
            () -> reservationService.changeReservationStatus(
                reservation.getId(), ReservationStatus.CANCELLED, otherBusiness
            )
        );

        assertEquals("You are not authorized to change this reservation's status", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when reservation not found")
    void changeReservationStatus_ReservationNotFound_ThrowsIllegalArgument() {
        Customer customer = createCustomer("NotFoundCustomer", "notfoundcustomer@smartdine.com", 200000007L);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationService.changeReservationStatus(
                99999L, ReservationStatus.CANCELLED, customer
            )
        );

        assertEquals("Reservation not found with id: 99999", exception.getMessage());
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
