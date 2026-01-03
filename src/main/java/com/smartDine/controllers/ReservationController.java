package com.smartDine.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.ProfileDTO;
import com.smartDine.dto.ReservationDTO;
import com.smartDine.dto.ReservationDetailsDTO;
import com.smartDine.dto.RestaurantReservationDTO;
import com.smartDine.dto.UpdateReservationStatusDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.Role;
import com.smartDine.entity.User;
import com.smartDine.services.CustomerService;
import com.smartDine.services.ReservationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/smartdine/api")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;
    private final CustomerService customerService;

    public ReservationController(
            ReservationService reservationService,
            CustomerService customerService) {
        this.reservationService = reservationService;
        this.customerService = customerService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationDTO> createReservation(
        @Valid @RequestBody ReservationDTO reservationDTO,
        @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Customer customer = customerService.getCustomerById(user.getId());
        Reservation created = reservationService.createReservation(reservationDTO, customer);
        ReservationDTO response = ReservationDTO.fromEntity(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/reservations/{reservationId}")
    public ResponseEntity<ReservationDTO> updateReservationStatus(
        @PathVariable Long reservationId,
        @Valid @RequestBody UpdateReservationStatusDTO dto,
        @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Reservation updated = reservationService.changeReservationStatus(
            reservationId, dto.getStatus(), user
        );
        return ResponseEntity.ok(ReservationDTO.fromEntity(updated));
    }

    @GetMapping("/me/reservations")
    public ResponseEntity<List<ReservationDetailsDTO>> getMyReservations(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Customer customer = customerService.getCustomerById(user.getId());
        List<Reservation> allReservations = reservationService.getAllReservationsForCustomer(customer.getId());
        List<ReservationDetailsDTO> response = ReservationDetailsDTO.fromEntity(allReservations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurants/{id}/reservations")
    public ResponseEntity<List<RestaurantReservationDTO>> getRestaurantReservations(
        @PathVariable Long id,
        @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() != Role.ROLE_BUSINESS) {
            throw new BadCredentialsException("Only business owners can access restaurant reservations");
        }

        Business business = (Business) user;
        List<Reservation> reservations = reservationService.getReservationsByRestaurantAndDate(id, date, business);
        List<RestaurantReservationDTO> response = RestaurantReservationDTO.fromEntity(reservations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reservations/{id}/participants")
    public ResponseEntity<List<ProfileDTO>> getReservationParticipants(
        @PathVariable Long id,
        @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Customer customer = customerService.getCustomerById(user.getId());
        List<Customer> participants = reservationService.getReservationParticipants(id, customer.getId());
        List<ProfileDTO> response = ProfileDTO.fromEntity(participants);
        return ResponseEntity.ok(response);
    }
}
