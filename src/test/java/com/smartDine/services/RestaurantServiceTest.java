package com.smartDine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.smartDine.dto.RestaurantDTO;
import com.smartDine.entity.Restaurant;
import com.smartDine.repository.RestaurantRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class RestaurantServiceTest {
    @Autowired
    private RestaurantService restaurantService;
    @Autowired 
    private RestaurantRepository restaurantRepository;

    @Test
    @DisplayName("Context loads and RestaurantService is not null")
    void contextLoads() {
        assertNotNull(restaurantService);
    }

    @Test
    @DisplayName("Should create a new restaurant successfully")
    void testCreateRestaurant() {
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Test Restaurant");
        restaurantDTO.setAddress("123 Test St");
        restaurantDTO.setDescription("A test restaurant");

        Restaurant createdRestaurant = restaurantService.createRestaurant(restaurantDTO);

        assertNotNull(createdRestaurant);
        assertNotNull(createdRestaurant.getId());
        assertEquals("Test Restaurant", createdRestaurant.getName());
        assertEquals("123 Test St", createdRestaurant.getAddress());
        assertEquals("A test restaurant", createdRestaurant.getDescription());
    }

    @Test
    @DisplayName("Should throw exception when creating restaurant with duplicate name")
    void testCreateRestaurantWithExistingName() {
        // Create first restaurant
        RestaurantDTO restaurantDTO1 = new RestaurantDTO();
        restaurantDTO1.setName("Duplicate Restaurant");
        restaurantDTO1.setAddress("123 First St");
        restaurantDTO1.setDescription("First restaurant");
        restaurantService.createRestaurant(restaurantDTO1);

        // Try to create second restaurant with same name
        RestaurantDTO restaurantDTO2 = new RestaurantDTO();
        restaurantDTO2.setName("Duplicate Restaurant");
        restaurantDTO2.setAddress("456 Second St");
        restaurantDTO2.setDescription("Second restaurant");
        assertThrows(IllegalArgumentException.class, () -> {
            restaurantService.createRestaurant(restaurantDTO2);
        });

    }
    @Test
    @DisplayName("Should fetch restaurant by ID successfully")
    void testGetRestaurantById() {
        // Create a restaurant to retrieve
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Fetch Restaurant");
        restaurantDTO.setAddress("789 Fetch St");
        restaurantDTO.setDescription("A fetch test restaurant");
        Restaurant createdRestaurant = restaurantService.createRestaurant(restaurantDTO);

        // Retrieve the restaurant by ID
        Restaurant fetchedRestaurant = restaurantService.getRestaurantById(createdRestaurant.getId());

        assertNotNull(fetchedRestaurant);
        assertEquals(createdRestaurant.getId(), fetchedRestaurant.getId());
        assertEquals("Fetch Restaurant", fetchedRestaurant.getName());
    }
    @Test
    @DisplayName("Should throw exception when fetching non-existing restaurant by ID")
    void testGetRestaurantById_NotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            restaurantService.getRestaurantById(9999L); // Assuming this ID does not exist
        });
    }
    @Test
    @DisplayName("Should fetch restaurant by name successfully")
    void testGetRestaurantByNameFound() {
        // Create a restaurant to retrieve
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Unique Name Restaurant");
        restaurantDTO.setAddress("101 Unique St");
        restaurantDTO.setDescription("A unique name test restaurant");
        Restaurant createdRestaurant = restaurantService.createRestaurant(restaurantDTO);

        // Retrieve the restaurant by ID
        Restaurant fetchedRestaurant = restaurantService.getRestaurantById(createdRestaurant.getId());

        assertNotNull(fetchedRestaurant);
        assertEquals(createdRestaurant.getId(), fetchedRestaurant.getId());
        assertEquals("Unique Name Restaurant", fetchedRestaurant.getName());
    }
    @Test
    @DisplayName("Should delete existing restaurant successfully")
    void deleteExistingRestaurant_shouldRemoveRestaurant() {
        // Arrange
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Delete Me Restaurant");
        restaurantDTO.setAddress("321 Delete St");
        restaurantDTO.setDescription("A restaurant to be deleted");

        Restaurant savedRestaurant = restaurantService.createRestaurant(restaurantDTO);
        Long restaurantId = savedRestaurant.getId();

        // Act
        restaurantService.deleteRestaurant(restaurantId);

        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            restaurantService.getRestaurantById(restaurantId);
        });
    }
    @Test
    @DisplayName("Should throw exception when deleting non-existing restaurant")
    void deleteNonExistingRestaurant_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            restaurantService.deleteRestaurant(9999L); // Assuming this ID does not exist
        });
    }
    @Test
    void updateExistingRestaurant_shouldModifyDetails() {
        // Arrange
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Original Name");
        restaurantDTO.setAddress("111 Original St");
        restaurantDTO.setDescription("Original description");

        Restaurant savedRestaurant = restaurantService.createRestaurant(restaurantDTO);
        Long restaurantId = savedRestaurant.getId();

        // Prepare updated details
        RestaurantDTO updatedDTO = new RestaurantDTO();
        updatedDTO.setName("Updated Name");
        updatedDTO.setAddress("222 Updated St");
        updatedDTO.setDescription("Updated description");

        // Act
        Restaurant updatedRestaurant = restaurantService.updateRestaurant(restaurantId, updatedDTO);

        // Assert
        assertNotNull(updatedRestaurant);
        assertEquals(restaurantId, updatedRestaurant.getId());
        assertEquals("Updated Name", updatedRestaurant.getName());
        assertEquals("222 Updated St", updatedRestaurant.getAddress());
        assertEquals("Updated description", updatedRestaurant.getDescription());
    }
    @Test
    @DisplayName("Should throw exception when updating non-existing restaurant")
    void updateNonExistingRestaurant_shouldThrowException() {
        // Prepare updated details
        RestaurantDTO updatedDTO = new RestaurantDTO();
        updatedDTO.setName("Non-Existent Name");
        updatedDTO.setAddress("999 Nowhere St");
        updatedDTO.setDescription("This restaurant does not exist");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            restaurantService.updateRestaurant(9999L, updatedDTO); // Assuming this ID does not exist
        });
    }
}