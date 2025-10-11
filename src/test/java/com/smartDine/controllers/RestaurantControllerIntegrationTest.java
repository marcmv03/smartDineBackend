package com.smartDine.controllers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.RestaurantDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.User;
import com.smartDine.services.BusinessService;
import com.smartDine.services.RestaurantService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Transactional
public class RestaurantControllerIntegrationTest {

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private BusinessService businessService;

    @InjectMocks
    private RestaurantController restaurantController;

    private Restaurant sampleRestaurant;

    @BeforeEach
    void setUp() {
        sampleRestaurant = new Restaurant();
        sampleRestaurant.setId(1L);
        sampleRestaurant.setName("Testaurant");
        sampleRestaurant.setAddress("123 Test Ave");
    }

    @Test
    public void getRestaurants_publicEndpoint_shouldReturnList() {
        when(restaurantService.getRestaurants(null)).thenReturn(List.of(sampleRestaurant));

        ResponseEntity<List<Restaurant>> resp = restaurantController.getRestaurants(null);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().size() == 1);
        assertEquals("Testaurant", resp.getBody().get(0).getName());
    }

    @Test
    public void getRestaurantById_forbiddenForCustomer_shouldReturn403() {
        // create a customer-like User
        User customer = new Business(); // Business extends User but we set role to customer to simulate
        customer.setRole("customer");

        ResponseEntity<Restaurant> resp = restaurantController.getRestaurantById(1L, customer);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    public void createRestaurant_asBusiness_shouldReturnCreated() {
        Business owner = new Business();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        owner.setPhoneNumber(123456789L);

        RestaurantDTO dto = new RestaurantDTO();
        dto.setName("New Resto");
        dto.setAddress("Addr");

        Restaurant created = new Restaurant();
        created.setId(2L);
        created.setName(dto.getName());
        created.setAddress(dto.getAddress());

        when(businessService.createRestaurantForBusiness(any(Business.class), any(RestaurantDTO.class))).thenReturn(created);

        ResponseEntity<Restaurant> resp = restaurantController.createRestaurant(dto, owner);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2L, resp.getBody().getId());
    }

    @Test
    public void createRestaurant_asNonBusiness_shouldReturnForbidden() {
        User notBusiness = new Customer();
        RestaurantDTO dto = new RestaurantDTO();
        dto.setName("Should Fail");

        ResponseEntity<Restaurant> resp = restaurantController.createRestaurant(dto, notBusiness);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }
}
