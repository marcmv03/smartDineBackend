package com.smartDine.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.ReservationParticipation;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.TimeSlot;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.ReservationRepository;
import com.smartDine.repository.RestaurantRepository;
import com.smartDine.repository.RestaurantTableRepository;
import com.smartDine.repository.TimeSlotRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReservationParticipationServiceTest {

    @Autowired
    private ReservationParticipationService participationService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    private Customer customer1;
    private Customer customer2;
    private Customer customer3;
    private Reservation reservation1;
    private Reservation reservation2;

    @BeforeEach
    void setUp() {
        // Create customers
        customer1 = new Customer("Customer 1", "customer1@test.com", "password", 111111111L);
        customer1 = customerRepository.save(customer1);

        customer2 = new Customer("Customer 2", "customer2@test.com", "password", 222222222L);
        customer2 = customerRepository.save(customer2);

        customer3 = new Customer("Customer 3", "customer3@test.com", "password", 333333333L);
        customer3 = customerRepository.save(customer3);

        // Create business and restaurant
        Business business = new Business("Business", "business@test.com", "password", 444444444L);
        business = businessRepository.save(business);

        Restaurant restaurant = new Restaurant("Test Restaurant", "Test Address", "Test Description");
        restaurant.setOwner(business);
        restaurant = restaurantRepository.save(restaurant);

        // Create time slot
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(12.0);
        timeSlot.setEndTime(14.0);
        timeSlot.setDayOfWeek(DayOfWeek.MONDAY);
        timeSlot.setRestaurant(restaurant);
        timeSlot = timeSlotRepository.save(timeSlot);

        // Create table
        RestaurantTable table = new RestaurantTable(1, 6, false);
        table.setRestaurant(restaurant);
        table = tableRepository.save(table);

        // Create reservations
        reservation1 = new Reservation();
        reservation1.setCustomer(customer1);
        reservation1.setRestaurant(restaurant);
        reservation1.setTimeSlot(timeSlot);
        reservation1.setRestaurantTable(table);
        reservation1.setNumGuests(4);
        reservation1.setDate(LocalDate.now().plusDays(7));
        reservation1.setCreatedAt(LocalDate.now());
        reservation1 = reservationRepository.save(reservation1);

        reservation2 = new Reservation();
        reservation2.setCustomer(customer2);
        reservation2.setRestaurant(restaurant);
        reservation2.setTimeSlot(timeSlot);
        reservation2.setRestaurantTable(table);
        reservation2.setNumGuests(2);
        reservation2.setDate(LocalDate.now().plusDays(14));
        reservation2.setCreatedAt(LocalDate.now());
        reservation2 = reservationRepository.save(reservation2);
    }

    @Nested
    @DisplayName("createNewParticipation tests")
    class CreateNewParticipationTests {

        @Test
        @DisplayName("Should create participation successfully")
        void shouldCreateParticipationSuccessfully() {
            ReservationParticipation participation = participationService.createNewParticipation(
                    customer2.getId(), reservation1.getId());

            assertNotNull(participation);
            assertNotNull(participation.getId());
            assertEquals(customer2.getId(), participation.getCustomer().getId());
            assertEquals(reservation1.getId(), participation.getReservation().getId());
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            Long nonExistentUserId = 99999L;

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> participationService.createNewParticipation(nonExistentUserId, reservation1.getId())
            );

            assertTrue(exception.getMessage().contains("Customer not found"));
        }

        @Test
        @DisplayName("Should throw exception when reservation not found")
        void shouldThrowExceptionWhenReservationNotFound() {
            Long nonExistentReservationId = 99999L;

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> participationService.createNewParticipation(customer1.getId(), nonExistentReservationId)
            );

            assertTrue(exception.getMessage().contains("Reservation not found"));
        }

        @Test
        @DisplayName("Should throw exception when user already participates")
        void shouldThrowExceptionWhenUserAlreadyParticipates() {
            // First participation - should succeed
            participationService.createNewParticipation(customer2.getId(), reservation1.getId());

            // Second participation with same user and reservation - should fail
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> participationService.createNewParticipation(customer2.getId(), reservation1.getId())
            );

            assertTrue(exception.getMessage().contains("already participates"));
        }

        @Test
        @DisplayName("Should allow same user to participate in different reservations")
        void shouldAllowSameUserInDifferentReservations() {
            ReservationParticipation participation1 = participationService.createNewParticipation(
                    customer3.getId(), reservation1.getId());
            ReservationParticipation participation2 = participationService.createNewParticipation(
                    customer3.getId(), reservation2.getId());

            assertNotNull(participation1);
            assertNotNull(participation2);
            assertEquals(customer3.getId(), participation1.getCustomer().getId());
            assertEquals(customer3.getId(), participation2.getCustomer().getId());
            assertEquals(reservation1.getId(), participation1.getReservation().getId());
            assertEquals(reservation2.getId(), participation2.getReservation().getId());
        }

        @Test
        @DisplayName("Should allow different users to participate in same reservation")
        void shouldAllowDifferentUsersInSameReservation() {
            ReservationParticipation participation1 = participationService.createNewParticipation(
                    customer2.getId(), reservation1.getId());
            ReservationParticipation participation2 = participationService.createNewParticipation(
                    customer3.getId(), reservation1.getId());

            assertNotNull(participation1);
            assertNotNull(participation2);
            assertEquals(customer2.getId(), participation1.getCustomer().getId());
            assertEquals(customer3.getId(), participation2.getCustomer().getId());
        }
    }

    @Nested
    @DisplayName("getUserParticipations tests")
    class GetUserParticipationsTests {

        @Test
        @DisplayName("Should return empty list when user has no participations")
        void shouldReturnEmptyListWhenNoParticipations() {
            List<ReservationParticipation> participations = participationService.getUserParticipations(customer1.getId());

            assertNotNull(participations);
            assertTrue(participations.isEmpty());
        }

        @Test
        @DisplayName("Should return all participations for a user")
        void shouldReturnAllParticipationsForUser() {
            // Create participations
            participationService.createNewParticipation(customer3.getId(), reservation1.getId());
            participationService.createNewParticipation(customer3.getId(), reservation2.getId());

            List<ReservationParticipation> participations = participationService.getUserParticipations(customer3.getId());

            assertNotNull(participations);
            assertEquals(2, participations.size());
            assertTrue(participations.stream().allMatch(p -> p.getCustomer().getId().equals(customer3.getId())));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            Long nonExistentUserId = 99999L;

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> participationService.getUserParticipations(nonExistentUserId)
            );

            assertTrue(exception.getMessage().contains("Customer not found"));
        }

        @Test
        @DisplayName("Should return correct participations for specific user only")
        void shouldReturnParticipationsForSpecificUserOnly() {
            // Create participations for different users
            participationService.createNewParticipation(customer2.getId(), reservation1.getId());
            participationService.createNewParticipation(customer3.getId(), reservation1.getId());
            participationService.createNewParticipation(customer3.getId(), reservation2.getId());

            List<ReservationParticipation> customer2Participations = 
                    participationService.getUserParticipations(customer2.getId());
            List<ReservationParticipation> customer3Participations = 
                    participationService.getUserParticipations(customer3.getId());

            assertEquals(1, customer2Participations.size());
            assertEquals(2, customer3Participations.size());
        }
    }

    @Nested
    @DisplayName("getParticipants tests")
    class GetParticipantsTests {

        @Test
        @DisplayName("Should return empty list when reservation has no participants")
        void shouldReturnEmptyListWhenNoParticipants() {
            List<ReservationParticipation> participants = participationService.getParticipants(reservation1.getId());

            assertNotNull(participants);
            assertTrue(participants.isEmpty());
        }

        @Test
        @DisplayName("Should return all participants for a reservation")
        void shouldReturnAllParticipantsForReservation() {
            // Add participants
            participationService.createNewParticipation(customer2.getId(), reservation1.getId());
            participationService.createNewParticipation(customer3.getId(), reservation1.getId());

            List<ReservationParticipation> participants = participationService.getParticipants(reservation1.getId());

            assertNotNull(participants);
            assertEquals(2, participants.size());
            assertTrue(participants.stream()
                    .allMatch(p -> p.getReservation().getId().equals(reservation1.getId())));
        }

        @Test
        @DisplayName("Should throw exception when reservation not found")
        void shouldThrowExceptionWhenReservationNotFound() {
            Long nonExistentReservationId = 99999L;

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> participationService.getParticipants(nonExistentReservationId)
            );

            assertTrue(exception.getMessage().contains("Reservation not found"));
        }

        @Test
        @DisplayName("Should return correct participants for specific reservation only")
        void shouldReturnParticipantsForSpecificReservationOnly() {
            // Add participants to different reservations
            participationService.createNewParticipation(customer2.getId(), reservation1.getId());
            participationService.createNewParticipation(customer3.getId(), reservation1.getId());
            participationService.createNewParticipation(customer1.getId(), reservation2.getId());

            List<ReservationParticipation> reservation1Participants = 
                    participationService.getParticipants(reservation1.getId());
            List<ReservationParticipation> reservation2Participants = 
                    participationService.getParticipants(reservation2.getId());

            assertEquals(2, reservation1Participants.size());
            assertEquals(1, reservation2Participants.size());
        }
    }

    @Nested
    @DisplayName("isParticipant tests")
    class IsParticipantTests {

        @Test
        @DisplayName("Should return false when user is not a participant")
        void shouldReturnFalseWhenNotParticipant() {
            boolean result = participationService.isParticipant(customer2.getId(), reservation1.getId());

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true when user is a participant")
        void shouldReturnTrueWhenParticipant() {
            participationService.createNewParticipation(customer2.getId(), reservation1.getId());

            boolean result = participationService.isParticipant(customer2.getId(), reservation1.getId());

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false for non-existent user")
        void shouldReturnFalseForNonExistentUser() {
            Long nonExistentUserId = 99999L;

            boolean result = participationService.isParticipant(nonExistentUserId, reservation1.getId());

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for non-existent reservation")
        void shouldReturnFalseForNonExistentReservation() {
            Long nonExistentReservationId = 99999L;

            boolean result = participationService.isParticipant(customer1.getId(), nonExistentReservationId);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should correctly identify participant across multiple reservations")
        void shouldCorrectlyIdentifyParticipantAcrossReservations() {
            participationService.createNewParticipation(customer2.getId(), reservation1.getId());
            // customer2 participates in reservation1 but not reservation2

            assertTrue(participationService.isParticipant(customer2.getId(), reservation1.getId()));
            assertFalse(participationService.isParticipant(customer2.getId(), reservation2.getId()));
        }
    }
}
