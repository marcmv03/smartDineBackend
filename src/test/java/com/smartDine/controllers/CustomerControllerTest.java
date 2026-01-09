package com.smartDine.controllers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.smartDine.dto.CustomerSearchDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.services.CustomerService;
import com.smartDine.services.FriendshipRequestService;
import com.smartDine.services.FriendshipService;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private FriendshipRequestService friendshipRequestService;

    @InjectMocks
    private CustomerController customerController;

    private Customer authenticatedCustomer;
    private Customer otherCustomer1;
    private Customer otherCustomer2;
    private Business business;

    @BeforeEach
    void setUp() {
        authenticatedCustomer = new Customer();
        authenticatedCustomer.setId(1L);
        authenticatedCustomer.setName("Authenticated User");
        authenticatedCustomer.setEmail("auth@test.com");
        authenticatedCustomer.setPhoneNumber(111111111L);

        otherCustomer1 = new Customer();
        otherCustomer1.setId(2L);
        otherCustomer1.setName("Juan García");
        otherCustomer1.setEmail("juan@test.com");
        otherCustomer1.setPhoneNumber(222222222L);

        otherCustomer2 = new Customer();
        otherCustomer2.setId(3L);
        otherCustomer2.setName("Juan Martínez");
        otherCustomer2.setEmail("juanm@test.com");
        otherCustomer2.setPhoneNumber(333333333L);

        business = new Business();
        business.setId(4L);
        business.setName("Test Business");
        business.setEmail("business@test.com");
    }

    @Nested
    @DisplayName("Search Customers - GET /customers")
    class SearchCustomersTests {

        @Test
        @DisplayName("Should return UNAUTHORIZED when user is null")
        void searchCustomersUnauthorized() {
            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers("juan", null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return FORBIDDEN when user is Business")
        void searchCustomersForbiddenForBusiness() {
            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers("juan", business);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return BAD_REQUEST when search term is null")
        void searchCustomersBadRequestNullName() {
            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers(null, authenticatedCustomer);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return BAD_REQUEST when search term is empty")
        void searchCustomersBadRequestEmptyName() {
            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers("", authenticatedCustomer);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return BAD_REQUEST when search term is less than 2 characters")
        void searchCustomersBadRequestShortName() {
            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers("a", authenticatedCustomer);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return BAD_REQUEST when search term is only whitespace")
        void searchCustomersBadRequestWhitespaceName() {
            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers("   ", authenticatedCustomer);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return empty list when no customers match")
        void searchCustomersNoResults() {
            when(customerService.getCustomerById(1L)).thenReturn(authenticatedCustomer);
            when(customerService.searchCustomers("xyz", 1L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers("xyz", authenticatedCustomer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("Should return customers matching search term")
        void searchCustomersSuccess() {
            when(customerService.getCustomerById(1L)).thenReturn(authenticatedCustomer);
            when(customerService.searchCustomers("juan", 1L)).thenReturn(List.of(otherCustomer1, otherCustomer2));
            when(friendshipService.areFriends(authenticatedCustomer, otherCustomer1)).thenReturn(false);
            when(friendshipService.areFriends(authenticatedCustomer, otherCustomer2)).thenReturn(false);
            when(friendshipRequestService.hasPendingRequestBetween(authenticatedCustomer, otherCustomer1)).thenReturn(false);
            when(friendshipRequestService.hasPendingRequestBetween(authenticatedCustomer, otherCustomer2)).thenReturn(false);

            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers("juan", authenticatedCustomer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            
            CustomerSearchDTO first = response.getBody().get(0);
            assertEquals(2L, first.getId());
            assertEquals("Juan García", first.getName());
            assertEquals("juan@test.com", first.getEmail());
            assertFalse(first.isFriend());
            assertFalse(first.isHasPendingRequest());
        }

        @Test
        @DisplayName("Should mark customer as friend when they are friends")
        void searchCustomersWithFriend() {
            when(customerService.getCustomerById(1L)).thenReturn(authenticatedCustomer);
            when(customerService.searchCustomers("juan", 1L)).thenReturn(List.of(otherCustomer1));
            when(friendshipService.areFriends(authenticatedCustomer, otherCustomer1)).thenReturn(true);
            when(friendshipRequestService.hasPendingRequestBetween(authenticatedCustomer, otherCustomer1)).thenReturn(false);

            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers("juan", authenticatedCustomer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            
            CustomerSearchDTO dto = response.getBody().get(0);
            assertTrue(dto.isFriend());
            assertFalse(dto.isHasPendingRequest());
        }

        @Test
        @DisplayName("Should mark customer as having pending request when request exists")
        void searchCustomersWithPendingRequest() {
            when(customerService.getCustomerById(1L)).thenReturn(authenticatedCustomer);
            when(customerService.searchCustomers("juan", 1L)).thenReturn(List.of(otherCustomer1));
            when(friendshipService.areFriends(authenticatedCustomer, otherCustomer1)).thenReturn(false);
            when(friendshipRequestService.hasPendingRequestBetween(authenticatedCustomer, otherCustomer1)).thenReturn(true);

            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers("juan", authenticatedCustomer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            
            CustomerSearchDTO dto = response.getBody().get(0);
            assertFalse(dto.isFriend());
            assertTrue(dto.isHasPendingRequest());
        }

        @Test
        @DisplayName("Should trim search term before searching")
        void searchCustomersTrimsSearchTerm() {
            when(customerService.getCustomerById(1L)).thenReturn(authenticatedCustomer);
            when(customerService.searchCustomers("juan", 1L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers("  juan  ", authenticatedCustomer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(customerService).searchCustomers("juan", 1L);
        }

        @Test
        @DisplayName("Should accept search term with exactly 2 characters")
        void searchCustomersMinimumLength() {
            when(customerService.getCustomerById(1L)).thenReturn(authenticatedCustomer);
            when(customerService.searchCustomers("ju", 1L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<CustomerSearchDTO>> response = customerController.searchCustomers("ju", authenticatedCustomer);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(customerService).searchCustomers("ju", 1L);
        }

        @Test
        @DisplayName("Should exclude authenticated user from results (verified by service call)")
        void searchCustomersExcludesAuthenticatedUser() {
            when(customerService.getCustomerById(1L)).thenReturn(authenticatedCustomer);
            when(customerService.searchCustomers("test", 1L)).thenReturn(Collections.emptyList());

            customerController.searchCustomers("test", authenticatedCustomer);

            // Verify that searchCustomers was called with the authenticated user's ID to exclude
            verify(customerService).searchCustomers("test", 1L);
        }
    }
}
