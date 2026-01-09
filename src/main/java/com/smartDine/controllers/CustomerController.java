package com.smartDine.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.CustomerSearchDTO;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Role;
import com.smartDine.entity.User;
import com.smartDine.services.CustomerService;
import com.smartDine.services.FriendshipRequestService;
import com.smartDine.services.FriendshipService;

/**
 * Controller for customer-related operations.
 * Provides endpoints for searching customers.
 */
@RestController
@RequestMapping("/smartdine/api")
public class CustomerController {

    private final CustomerService customerService;
    private final FriendshipService friendshipService;
    private final FriendshipRequestService friendshipRequestService;

    public CustomerController(
            CustomerService customerService,
            FriendshipService friendshipService,
            FriendshipRequestService friendshipRequestService) {
        this.customerService = customerService;
        this.friendshipService = friendshipService;
        this.friendshipRequestService = friendshipRequestService;
    }

    /**
     * Search for customers by name.
     * GET /smartdine/api/customers?name=X
     * 
     * Returns customers matching the search term (case-insensitive, partial match).
     * The authenticated user is excluded from results.
     * Results include friendship status (isFriend) and pending request status (hasPendingRequest).
     * 
     * @param name The search term (minimum 2 characters required)
     * @param user The authenticated user
     * @return List of matching customers with friendship information
     */
    @GetMapping("/customers")
    public ResponseEntity<List<CustomerSearchDTO>> searchCustomers(
            @RequestParam String name,
            @AuthenticationPrincipal User user
    ) {
        // Check authentication
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Only customers can search for other customers
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Validate minimum search length
        if (name == null || name.trim().length() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Get the authenticated customer
        Customer authenticatedCustomer = customerService.getCustomerById(user.getId());

        // Search for customers (excluding the authenticated user)
        List<Customer> customers = customerService.searchCustomers(name.trim(), user.getId());

        // Convert to DTOs and enrich with friendship information
        List<CustomerSearchDTO> results = customers.stream()
                .map(customer -> {
                    CustomerSearchDTO dto = CustomerSearchDTO.fromEntity(customer);
                    
                    // Check if they are friends
                    dto.setFriend(friendshipService.areFriends(authenticatedCustomer, customer));
                    
                    // Check if there's a pending friend request between them
                    dto.setHasPendingRequest(
                        friendshipRequestService.hasPendingRequestBetween(authenticatedCustomer, customer)
                    );
                    
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }
}
