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
import com.smartDine.entity.ReservationParticipation;
import com.smartDine.entity.ReservationStatus;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.TimeSlot;
import com.smartDine.exceptions.ExpiredOpenReservationException;
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

    @Autowired
    private FriendshipService friendshipService;

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

    // ==================== addParticipantToReservation Tests ====================

    @Test
    @DisplayName("Should add participant to reservation successfully")
    void addParticipantToReservation_Success() {
        Business owner = createBusiness("OwnerParticipant1", "ownerparticipant1@smartdine.com", 300000001L);
        Restaurant restaurant = createRestaurant(owner, "Participant Restaurant 1");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.MONDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 1, 6);
        Customer creator = createCustomer("Creator1", "creator1@smartdine.com", 400000001L);
        Customer joiner = createCustomer("Joiner1", "joiner1@smartdine.com", 400000002L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(1));

        Reservation reservation = reservationService.createReservation(dto, creator);

        reservationService.addParticipantToReservation(reservation.getId(), joiner, 3);

        // Verify using ReservationParticipationService
        Reservation updated = reservationService.getReservationById(reservation.getId());
        assertEquals(true, reservationService.isParticipant(updated, joiner));
        assertEquals(2, reservationService.getTotalParticipantsCount(updated)); // 1 creator + 1 participant
    }

    @Test
    @DisplayName("Should throw ExpiredOpenReservationException when reservation date has passed")
    void addParticipantToReservation_ExpiredReservation_ThrowsException() {
        Business owner = createBusiness("OwnerParticipant2", "ownerparticipant2@smartdine.com", 300000002L);
        Restaurant restaurant = createRestaurant(owner, "Participant Restaurant 2");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.MONDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 1, 6);
        Customer creator = createCustomer("Creator2", "creator2@smartdine.com", 400000003L);
        Customer joiner = createCustomer("Joiner2", "joiner2@smartdine.com", 400000004L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().minusDays(1)); // Past date

        Reservation reservation = reservationService.createReservation(dto, creator);

        ExpiredOpenReservationException exception = assertThrows(
            ExpiredOpenReservationException.class,
            () -> reservationService.addParticipantToReservation(reservation.getId(), joiner, 3)
        );

        assertEquals("Cannot join reservation: the reservation date has already passed", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when customer is already a participant")
    void addParticipantToReservation_AlreadyParticipant_ThrowsException() {
        Business owner = createBusiness("OwnerParticipant3", "ownerparticipant3@smartdine.com", 300000003L);
        Restaurant restaurant = createRestaurant(owner, "Participant Restaurant 3");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.MONDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 1, 6);
        Customer creator = createCustomer("Creator3", "creator3@smartdine.com", 400000005L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(1));

        Reservation reservation = reservationService.createReservation(dto, creator);

        // Creator tries to join again
        IllegalReservationStateChangeException exception = assertThrows(
            IllegalReservationStateChangeException.class,
            () -> reservationService.addParticipantToReservation(reservation.getId(), creator, 3)
        );

        assertEquals("You are already a participant in this reservation", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when no available slots")
    void addParticipantToReservation_NoAvailableSlots_ThrowsException() {
        Business owner = createBusiness("OwnerParticipant4", "ownerparticipant4@smartdine.com", 300000004L);
        Restaurant restaurant = createRestaurant(owner, "Participant Restaurant 4");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.MONDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 1, 6);
        Customer creator = createCustomer("Creator4", "creator4@smartdine.com", 400000006L);
        Customer joiner1 = createCustomer("Joiner3", "joiner3@smartdine.com", 400000007L);
        Customer joiner2 = createCustomer("Joiner4", "joiner4@smartdine.com", 400000008L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(1));

        Reservation reservation = reservationService.createReservation(dto, creator);

        // Add first joiner (maxParticipants = 1)
        reservationService.addParticipantToReservation(reservation.getId(), joiner1, 1);

        // Try to add second joiner when max is already reached
        IllegalReservationStateChangeException exception = assertThrows(
            IllegalReservationStateChangeException.class,
            () -> reservationService.addParticipantToReservation(reservation.getId(), joiner2, 1)
        );

        assertEquals("No available slots: reservation is full", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when reservation is cancelled")
    void addParticipantToReservation_CancelledReservation_ThrowsException() {
        Business owner = createBusiness("OwnerParticipant5", "ownerparticipant5@smartdine.com", 300000005L);
        Restaurant restaurant = createRestaurant(owner, "Participant Restaurant 5");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.MONDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 1, 6);
        Customer creator = createCustomer("Creator5", "creator5@smartdine.com", 400000009L);
        Customer joiner = createCustomer("Joiner5", "joiner5@smartdine.com", 400000010L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(1));

        Reservation reservation = reservationService.createReservation(dto, creator);
        
        // Cancel the reservation
        reservationService.changeReservationStatus(reservation.getId(), ReservationStatus.CANCELLED, creator);

        // Try to join cancelled reservation
        IllegalReservationStateChangeException exception = assertThrows(
            IllegalReservationStateChangeException.class,
            () -> reservationService.addParticipantToReservation(reservation.getId(), joiner, 3)
        );

        assertEquals("Cannot join reservation: reservation is CANCELLED", exception.getMessage());
    }

    @Test
    @DisplayName("getAllReservationsForCustomer returns only owned reservations when no participations")
    void getAllReservationsForCustomerOnlyOwned() {
        Business owner = createBusiness("Owner GetAll1", "ownergetall1@smartdine.com", 777777771L);
        Restaurant restaurant = createRestaurant(owner, "Restaurant GetAll1");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.MONDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 1, 4);
        Customer customer = createCustomer("Customer GetAll1", "customergetall1@smartdine.com", 888888881L);

        LocalDate reservationDate = LocalDate.now().plusDays(1);
        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(reservationDate);

        reservationService.createReservation(dto, customer);

        List<Reservation> allReservations = reservationService.getAllReservationsForCustomer(customer.getId());

        assertEquals(1, allReservations.size());
        assertEquals(customer.getId(), allReservations.get(0).getCustomer().getId());
    }

    @Test
    @DisplayName("getAllReservationsForCustomer returns both owned and participated reservations")
    void getAllReservationsForCustomerOwnedAndParticipated() {
        Business owner = createBusiness("Owner GetAll2", "ownergetall2@smartdine.com", 777777772L);
        Restaurant restaurant = createRestaurant(owner, "Restaurant GetAll2");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.TUESDAY, 18.0, 20.0);
        RestaurantTable table = createTable(restaurant, 2, 6);
        Customer customer1 = createCustomer("Customer GetAll2", "customergetall2@smartdine.com", 888888882L);
        Customer customer2 = createCustomer("Customer GetAll3", "customergetall3@smartdine.com", 888888883L);

        // Customer1 creates their own reservation
        LocalDate date1 = LocalDate.now().plusDays(1);
        ReservationDTO dto1 = new ReservationDTO();
        dto1.setRestaurantId(restaurant.getId());
        dto1.setTimeSlotId(timeSlot.getId());
        dto1.setTableId(table.getId());
        dto1.setNumCustomers(2);
        dto1.setDate(date1);
        reservationService.createReservation(dto1, customer1);

        // Customer2 creates a reservation and customer1 joins it
        LocalDate date2 = LocalDate.now().plusDays(2);
        ReservationDTO dto2 = new ReservationDTO();
        dto2.setRestaurantId(restaurant.getId());
        dto2.setTimeSlotId(timeSlot.getId());
        dto2.setTableId(table.getId());
        dto2.setNumCustomers(4);
        dto2.setDate(date2);
        Reservation reservation2 = reservationService.createReservation(dto2, customer2);
        reservationService.addParticipantToReservation(reservation2.getId(), customer1, 1);

        List<Reservation> allReservations = reservationService.getAllReservationsForCustomer(customer1.getId());

        assertEquals(2, allReservations.size());
        List<Long> customerIds = allReservations.stream()
                .map(r -> r.getCustomer().getId())
                .distinct()
                .sorted()
                .toList();
        assertEquals(List.of(customer1.getId(), customer2.getId()), customerIds);
    }

    @Test
    @DisplayName("getAllReservationsForCustomer prevents owner from being added as participant")
    void getAllReservationsForCustomerOwnerCannotBeParticipant() {
        Business owner = createBusiness("Owner GetAll3", "ownergetall3@smartdine.com", 777777773L);
        Restaurant restaurant = createRestaurant(owner, "Restaurant GetAll3");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.WEDNESDAY, 13.0, 15.0);
        RestaurantTable table = createTable(restaurant, 3, 8);
        Customer customer = createCustomer("Customer GetAll4", "customergetall4@smartdine.com", 888888884L);

        LocalDate reservationDate = LocalDate.now().plusDays(3);
        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(4);
        dto.setDate(reservationDate);

        Reservation reservation = reservationService.createReservation(dto, customer);
        
        // Attempt to add owner as participant should throw exception
        IllegalReservationStateChangeException exception = assertThrows(
            IllegalReservationStateChangeException.class,
            () -> reservationService.addParticipantToReservation(reservation.getId(), customer, 1)
        );
        
        assertEquals("You are already a participant in this reservation", exception.getMessage());
    }

    @Test
    @DisplayName("getAllReservationsForCustomer returns only participated reservations when customer owns none")
    void getAllReservationsForCustomerOnlyParticipated() {
        Business owner = createBusiness("Owner GetAll4", "ownergetall4@smartdine.com", 777777774L);
        Restaurant restaurant = createRestaurant(owner, "Restaurant GetAll4");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.THURSDAY, 19.0, 21.0);
        RestaurantTable table = createTable(restaurant, 4, 4);
        Customer reservationOwner = createCustomer("Owner Customer GetAll5", "ownergetall5@smartdine.com", 888888885L);
        Customer participant = createCustomer("Participant GetAll6", "participantgetall6@smartdine.com", 888888886L);

        LocalDate reservationDate = LocalDate.now().plusDays(4);
        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(3);
        dto.setDate(reservationDate);

        Reservation reservation = reservationService.createReservation(dto, reservationOwner);
        reservationService.addParticipantToReservation(reservation.getId(), participant, 1);

        List<Reservation> allReservations = reservationService.getAllReservationsForCustomer(participant.getId());

        assertEquals(1, allReservations.size());
        assertEquals(reservation.getId(), allReservations.get(0).getId());
        assertEquals(reservationOwner.getId(), allReservations.get(0).getCustomer().getId());
    }

    // ==================== addFriendAsParticipant Tests ====================

    @Test
    @DisplayName("Should add friend as participant successfully")
    void addFriendAsParticipant_Success() {
        Business owner = createBusiness("OwnerFriend1", "ownerfriend1@smartdine.com", 500000001L);
        Restaurant restaurant = createRestaurant(owner, "Friend Restaurant 1");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.MONDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 1, 6);
        Customer reservationOwner = createCustomer("ResOwner1", "resowner1@smartdine.com", 600000001L);
        Customer friend = createCustomer("Friend1", "friend1@smartdine.com", 600000002L);

        // Create friendship
        friendshipService.createFriendship(reservationOwner, friend);

        // Create reservation
        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(1));

        Reservation reservation = reservationService.createReservation(dto, reservationOwner);

        // Add friend as participant
        ReservationParticipation participation = reservationService.addFriendAsParticipant(
            reservation.getId(), friend.getId(), reservationOwner.getId()
        );

        assertNotNull(participation);
        assertEquals(friend.getId(), participation.getCustomer().getId());
        assertEquals(reservation.getId(), participation.getReservation().getId());
    }

    @Test
    @DisplayName("Should fail to add non-friend as participant")
    void addFriendAsParticipant_NotFriend_ThrowsException() {
        Business owner = createBusiness("OwnerFriend2", "ownerfriend2@smartdine.com", 500000002L);
        Restaurant restaurant = createRestaurant(owner, "Friend Restaurant 2");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.TUESDAY, 18.0, 20.0);
        RestaurantTable table = createTable(restaurant, 2, 4);
        Customer reservationOwner = createCustomer("ResOwner2", "resowner2@smartdine.com", 600000003L);
        Customer notFriend = createCustomer("NotFriend2", "notfriend2@smartdine.com", 600000004L);

        // No friendship created

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(2));

        Reservation reservation = reservationService.createReservation(dto, reservationOwner);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationService.addFriendAsParticipant(
                reservation.getId(), notFriend.getId(), reservationOwner.getId()
            )
        );

        assertEquals("You can only add friends as participants", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when non-owner tries to add participant")
    void addFriendAsParticipant_NonOwner_ThrowsException() {
        Business owner = createBusiness("OwnerFriend3", "ownerfriend3@smartdine.com", 500000003L);
        Restaurant restaurant = createRestaurant(owner, "Friend Restaurant 3");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.WEDNESDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 3, 4);
        Customer reservationOwner = createCustomer("ResOwner3", "resowner3@smartdine.com", 600000005L);
        Customer friend = createCustomer("Friend3", "friend3@smartdine.com", 600000006L);
        Customer otherCustomer = createCustomer("OtherCust3", "othercust3@smartdine.com", 600000007L);

        friendshipService.createFriendship(reservationOwner, friend);
        friendshipService.createFriendship(otherCustomer, friend);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(3));

        Reservation reservation = reservationService.createReservation(dto, reservationOwner);

        BadCredentialsException exception = assertThrows(
            BadCredentialsException.class,
            () -> reservationService.addFriendAsParticipant(
                reservation.getId(), friend.getId(), otherCustomer.getId() // Wrong owner
            )
        );

        assertEquals("Only the reservation owner can add participants", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when table capacity is exceeded")
    void addFriendAsParticipant_CapacityExceeded_ThrowsException() {
        Business owner = createBusiness("OwnerFriend4", "ownerfriend4@smartdine.com", 500000004L);
        Restaurant restaurant = createRestaurant(owner, "Friend Restaurant 4");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.THURSDAY, 19.0, 21.0);
        RestaurantTable table = createTable(restaurant, 4, 2); // Capacity of 2
        Customer reservationOwner = createCustomer("ResOwner4", "resowner4@smartdine.com", 600000008L);
        Customer friend1 = createCustomer("Friend4A", "friend4a@smartdine.com", 600000009L);
        Customer friend2 = createCustomer("Friend4B", "friend4b@smartdine.com", 600000010L);

        friendshipService.createFriendship(reservationOwner, friend1);
        friendshipService.createFriendship(reservationOwner, friend2);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(1);
        dto.setDate(LocalDate.now().plusDays(4));

        Reservation reservation = reservationService.createReservation(dto, reservationOwner);

        // Add first friend (capacity becomes 2)
        reservationService.addFriendAsParticipant(reservation.getId(), friend1.getId(), reservationOwner.getId());

        // Try to add second friend (capacity exceeded)
        IllegalReservationStateChangeException exception = assertThrows(
            IllegalReservationStateChangeException.class,
            () -> reservationService.addFriendAsParticipant(
                reservation.getId(), friend2.getId(), reservationOwner.getId()
            )
        );

        assertEquals("Cannot add participant: would exceed table capacity of 2", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when friend already a participant")
    void addFriendAsParticipant_AlreadyParticipant_ThrowsException() {
        Business owner = createBusiness("OwnerFriend5", "ownerfriend5@smartdine.com", 500000005L);
        Restaurant restaurant = createRestaurant(owner, "Friend Restaurant 5");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.FRIDAY, 18.0, 20.0);
        RestaurantTable table = createTable(restaurant, 5, 6);
        Customer reservationOwner = createCustomer("ResOwner5", "resowner5@smartdine.com", 600000011L);
        Customer friend = createCustomer("Friend5", "friend5@smartdine.com", 600000012L);

        friendshipService.createFriendship(reservationOwner, friend);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(5));

        Reservation reservation = reservationService.createReservation(dto, reservationOwner);

        // Add friend once
        reservationService.addFriendAsParticipant(reservation.getId(), friend.getId(), reservationOwner.getId());

        // Try to add again
        IllegalReservationStateChangeException exception = assertThrows(
            IllegalReservationStateChangeException.class,
            () -> reservationService.addFriendAsParticipant(
                reservation.getId(), friend.getId(), reservationOwner.getId()
            )
        );

        assertEquals("This user is already a participant in the reservation", exception.getMessage());
    }

    // ==================== removeParticipantFromReservation Tests ====================

    @Test
    @DisplayName("Owner should be able to remove participant")
    void removeParticipantFromReservation_OwnerRemovesParticipant_Success() {
        Business owner = createBusiness("OwnerRemove1", "ownerremove1@smartdine.com", 700000001L);
        Restaurant restaurant = createRestaurant(owner, "Remove Restaurant 1");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.MONDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 1, 4);
        Customer reservationOwner = createCustomer("ResOwnerRem1", "resownerrem1@smartdine.com", 800000001L);
        Customer friend = createCustomer("FriendRem1", "friendrem1@smartdine.com", 800000002L);

        friendshipService.createFriendship(reservationOwner, friend);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(1));

        Reservation reservation = reservationService.createReservation(dto, reservationOwner);
        reservationService.addFriendAsParticipant(reservation.getId(), friend.getId(), reservationOwner.getId());

        // Owner removes participant
        reservationService.removeParticipantFromReservation(
            reservation.getId(), friend.getId(), reservationOwner.getId()
        );

        // Verify participant was removed
        Reservation updated = reservationService.getReservationById(reservation.getId());
        assertEquals(1, reservationService.getTotalParticipantsCount(updated)); // Only owner
    }

    @Test
    @DisplayName("Participant should be able to remove themselves")
    void removeParticipantFromReservation_SelfRemoval_Success() {
        Business owner = createBusiness("OwnerRemove2", "ownerremove2@smartdine.com", 700000002L);
        Restaurant restaurant = createRestaurant(owner, "Remove Restaurant 2");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.TUESDAY, 18.0, 20.0);
        RestaurantTable table = createTable(restaurant, 2, 4);
        Customer reservationOwner = createCustomer("ResOwnerRem2", "resownerrem2@smartdine.com", 800000003L);
        Customer friend = createCustomer("FriendRem2", "friendrem2@smartdine.com", 800000004L);

        friendshipService.createFriendship(reservationOwner, friend);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(2));

        Reservation reservation = reservationService.createReservation(dto, reservationOwner);
        reservationService.addFriendAsParticipant(reservation.getId(), friend.getId(), reservationOwner.getId());

        // Friend removes themselves
        reservationService.removeParticipantFromReservation(
            reservation.getId(), friend.getId(), friend.getId()
        );

        // Verify participant was removed
        Reservation updated = reservationService.getReservationById(reservation.getId());
        assertEquals(1, reservationService.getTotalParticipantsCount(updated)); // Only owner
    }

    @Test
    @DisplayName("Should fail to remove reservation owner")
    void removeParticipantFromReservation_CannotRemoveOwner() {
        Business owner = createBusiness("OwnerRemove3", "ownerremove3@smartdine.com", 700000003L);
        Restaurant restaurant = createRestaurant(owner, "Remove Restaurant 3");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.WEDNESDAY, 12.0, 14.0);
        RestaurantTable table = createTable(restaurant, 3, 4);
        Customer reservationOwner = createCustomer("ResOwnerRem3", "resownerrem3@smartdine.com", 800000005L);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(3));

        Reservation reservation = reservationService.createReservation(dto, reservationOwner);

        IllegalReservationStateChangeException exception = assertThrows(
            IllegalReservationStateChangeException.class,
            () -> reservationService.removeParticipantFromReservation(
                reservation.getId(), reservationOwner.getId(), reservationOwner.getId()
            )
        );

        assertEquals("Cannot remove the reservation owner. Use cancel reservation instead.", exception.getMessage());
    }

    @Test
    @DisplayName("Non-owner non-participant should not be able to remove others")
    void removeParticipantFromReservation_UnauthorizedRemoval_ThrowsException() {
        Business owner = createBusiness("OwnerRemove4", "ownerremove4@smartdine.com", 700000004L);
        Restaurant restaurant = createRestaurant(owner, "Remove Restaurant 4");
        TimeSlot timeSlot = createTimeSlot(restaurant, DayOfWeek.THURSDAY, 19.0, 21.0);
        RestaurantTable table = createTable(restaurant, 4, 4);
        Customer reservationOwner = createCustomer("ResOwnerRem4", "resownerrem4@smartdine.com", 800000006L);
        Customer friend = createCustomer("FriendRem4", "friendrem4@smartdine.com", 800000007L);
        Customer otherCustomer = createCustomer("OtherCustRem4", "othercustrem4@smartdine.com", 800000008L);

        friendshipService.createFriendship(reservationOwner, friend);

        ReservationDTO dto = new ReservationDTO();
        dto.setRestaurantId(restaurant.getId());
        dto.setTimeSlotId(timeSlot.getId());
        dto.setTableId(table.getId());
        dto.setNumCustomers(2);
        dto.setDate(LocalDate.now().plusDays(4));

        Reservation reservation = reservationService.createReservation(dto, reservationOwner);
        reservationService.addFriendAsParticipant(reservation.getId(), friend.getId(), reservationOwner.getId());

        // Other customer (not owner, not the participant) tries to remove friend
        BadCredentialsException exception = assertThrows(
            BadCredentialsException.class,
            () -> reservationService.removeParticipantFromReservation(
                reservation.getId(), friend.getId(), otherCustomer.getId()
            )
        );

        assertEquals("Only the reservation owner or the participant themselves can remove a participant", exception.getMessage());
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
