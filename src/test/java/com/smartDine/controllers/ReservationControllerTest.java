package com.smartDine.controllers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.smartDine.dto.ReservationDTO;
import com.smartDine.dto.ReservationDetailsDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.TimeSlot;
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
}
