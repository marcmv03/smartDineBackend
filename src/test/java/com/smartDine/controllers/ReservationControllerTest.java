package com.smartDine.controllers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import com.smartDine.dto.ReservationDTO;
import com.smartDine.dto.ReservationDetailsDTO;
import com.smartDine.dto.RestaurantReservationDTO;
import com.smartDine.dto.UpdateReservationStatusDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.ReservationStatus;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.TimeSlot;
import com.smartDine.exceptions.IllegalReservationStateChangeException;
import com.smartDine.services.CustomerService;
import com.smartDine.services.ReservationService;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private ReservationController reservationController;

    private Customer customer;
    private Reservation reservation;
    private LocalDate reservationDate;

    @BeforeEach
    void setUp() {
        reservationDate = LocalDate.now().plusDays(1);
        
        customer = new Customer();
        customer.setId(1L);
        customer.setEmail("customer@smartdine.com");
        customer.setName("Customer");
        customer.setPhoneNumber(123456789L);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(10L);
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("123 Main Street");
        restaurant.setImageUrl("restaurants/10/images/test.jpg");

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(20L);
        timeSlot.setDayOfWeek(DayOfWeek.MONDAY);
        timeSlot.setStartTime(12.0);
        timeSlot.setEndTime(14.0);
        timeSlot.setRestaurant(restaurant);

        RestaurantTable table = new RestaurantTable();
        table.setId(30L);
        table.setNumber(1);
        table.setCapacity(4);
        table.setOutside(false);
        table.setRestaurant(restaurant);

        reservation = new Reservation();
        reservation.setId(40L);
        reservation.setCustomer(customer);
        reservation.setRestaurant(restaurant);
        reservation.setTimeSlot(timeSlot);
        reservation.setRestaurantTable(table);
        reservation.setNumGuests(2);
        reservation.setDate(reservationDate);
        reservation.setCreatedAt(LocalDate.now());
        reservation.setStatus(ReservationStatus.CONFIRMED);
    }

    @Test
    void createReservationAsCustomerReturnsCreated() {
        ReservationDTO request = new ReservationDTO();
        request.setRestaurantId(10L);
        request.setTimeSlotId(20L);
        request.setTableId(30L);
        request.setNumCustomers(2);
        request.setDate(reservationDate);

        when(customerService.getCustomerById(1L)).thenReturn(customer);
        when(reservationService.createReservation(eq(request), eq(customer))).thenReturn(reservation);

        ResponseEntity<ReservationDTO> response = reservationController.createReservation(request, customer);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(40L, response.getBody().getId());
        assertEquals(10L, response.getBody().getRestaurantId());
        assertEquals(20L, response.getBody().getTimeSlotId());
        assertEquals(30L, response.getBody().getTableId());
        assertEquals(2, response.getBody().getNumCustomers());
        assertEquals(reservationDate, response.getBody().getDate());
    }

    @Test
    void createReservationAsBusinessReturnsForbidden() {
        Business businessUser = new Business();
        businessUser.setId(2L);
        ResponseEntity<ReservationDTO> response = reservationController.createReservation(new ReservationDTO(), businessUser);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getMyReservationsReturnsListForCustomer() {
        when(customerService.getCustomerById(1L)).thenReturn(customer);
        when(reservationService.getReservationsForCustomer(1L)).thenReturn(List.of(reservation));

        ResponseEntity<List<ReservationDetailsDTO>> response = reservationController.getMyReservations(customer);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
        // Verificar que el DTO mapea correctamente todos los campos
        ReservationDetailsDTO dto = response.getBody().get(0);
        assertEquals(40L, dto.getReservationId());
        assertEquals("Test Restaurant", dto.getRestaurantName());
        assertEquals("restaurants/10/images/test.jpg", dto.getImageKey());
        assertEquals(reservationDate.toString(), dto.getReservationDate());
        assertEquals(12.0, dto.getStartTime());
        assertEquals(14.0, dto.getEndTime());
        assertEquals(2, dto.getNumberOfGuests());
        assertEquals("123 Main Street", dto.getAddress());
    }

    @Test
    void getMyReservationsAsBusinessReturnsForbidden() {
        Business businessUser = new Business();
        businessUser.setId(3L);
        ResponseEntity<List<ReservationDetailsDTO>> response = reservationController.getMyReservations(businessUser);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getRestaurantReservationsAsCustomerThrowsBadCredentials() {
        assertThrows(BadCredentialsException.class, () -> 
            reservationController.getRestaurantReservations(10L, reservationDate, customer)
        );
    }

    @Test
    void getRestaurantReservationsAsBusinessReturnsOk() {
        Business businessUser = new Business();
        businessUser.setId(5L);
        businessUser.setName("Business Owner");

        when(reservationService.getReservationsByRestaurantAndDate(eq(10L), eq(reservationDate), any(Business.class)))
            .thenReturn(List.of(reservation));

        ResponseEntity<List<RestaurantReservationDTO>> response = 
            reservationController.getRestaurantReservations(10L, reservationDate, businessUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
        RestaurantReservationDTO dto = response.getBody().get(0);
        assertEquals(40L, dto.getId());
        assertEquals("Customer", dto.getUsername());
        assertEquals(12.0, dto.getStartTime());
        assertEquals(14.0, dto.getEndTime());
        assertEquals(1, dto.getNumTable());
        assertEquals(false, dto.getOutside());
    }

    @Test
    void getRestaurantReservationsWithNullUserReturnsUnauthorized() {
        ResponseEntity<List<RestaurantReservationDTO>> response = 
            reservationController.getRestaurantReservations(10L, reservationDate, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ==================== updateReservationStatus Tests ====================

    @Test
    void updateReservationStatus_CustomerCancels_ReturnsOk() {
        UpdateReservationStatusDTO dto = new UpdateReservationStatusDTO(ReservationStatus.CANCELLED);
        
        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setId(40L);
        cancelledReservation.setCustomer(customer);
        cancelledReservation.setRestaurant(reservation.getRestaurant());
        cancelledReservation.setTimeSlot(reservation.getTimeSlot());
        cancelledReservation.setRestaurantTable(reservation.getRestaurantTable());
        cancelledReservation.setNumGuests(2);
        cancelledReservation.setDate(reservationDate);
        cancelledReservation.setStatus(ReservationStatus.CANCELLED);

        when(reservationService.changeReservationStatus(eq(40L), eq(ReservationStatus.CANCELLED), eq(customer)))
            .thenReturn(cancelledReservation);

        ResponseEntity<ReservationDTO> response = reservationController.updateReservationStatus(40L, dto, customer);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(40L, response.getBody().getId());
    }

    @Test
    void updateReservationStatus_BusinessCompletes_ReturnsOk() {
        Business businessUser = new Business();
        businessUser.setId(5L);
        businessUser.setName("Business Owner");

        UpdateReservationStatusDTO dto = new UpdateReservationStatusDTO(ReservationStatus.COMPLETED);
        
        Reservation completedReservation = new Reservation();
        completedReservation.setId(40L);
        completedReservation.setCustomer(customer);
        completedReservation.setRestaurant(reservation.getRestaurant());
        completedReservation.setTimeSlot(reservation.getTimeSlot());
        completedReservation.setRestaurantTable(reservation.getRestaurantTable());
        completedReservation.setNumGuests(2);
        completedReservation.setDate(reservationDate);
        completedReservation.setStatus(ReservationStatus.COMPLETED);

        when(reservationService.changeReservationStatus(eq(40L), eq(ReservationStatus.COMPLETED), eq(businessUser)))
            .thenReturn(completedReservation);

        ResponseEntity<ReservationDTO> response = reservationController.updateReservationStatus(40L, dto, businessUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(40L, response.getBody().getId());
    }

    @Test
    void updateReservationStatus_NullUser_ReturnsUnauthorized() {
        UpdateReservationStatusDTO dto = new UpdateReservationStatusDTO(ReservationStatus.CANCELLED);

        ResponseEntity<ReservationDTO> response = reservationController.updateReservationStatus(40L, dto, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void updateReservationStatus_IllegalStateChange_ThrowsException() {
        UpdateReservationStatusDTO dto = new UpdateReservationStatusDTO(ReservationStatus.COMPLETED);

        when(reservationService.changeReservationStatus(eq(40L), eq(ReservationStatus.COMPLETED), eq(customer)))
            .thenThrow(new IllegalReservationStateChangeException("Only the restaurant owner can mark a reservation as completed"));

        assertThrows(IllegalReservationStateChangeException.class, () ->
            reservationController.updateReservationStatus(40L, dto, customer)
        );
    }
}
