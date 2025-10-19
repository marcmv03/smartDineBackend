package com.smartDine.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.ReservationDTO;
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

    public ReservationController(ReservationService reservationService, CustomerService customerService) {
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

    @GetMapping("/me/reservations")
    public ResponseEntity<List<ReservationDTO>> getMyReservations(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Customer customer = customerService.getCustomerById(user.getId());
        List<Reservation> reservations = reservationService.getReservationsForCustomer(customer.getId());
        List<ReservationDTO> response = ReservationDTO.fromEntity(reservations);
        return ResponseEntity.ok(response);
    }
}
