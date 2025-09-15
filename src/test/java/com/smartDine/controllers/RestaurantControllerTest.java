package com.smartDine.controllers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.RestaurantDTO;
import com.smartDine.entity.Restaurant;
import com.smartDine.services.RestaurantService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RestaurantControllerTest {

    @Autowired
    private RestaurantService restaurantService;

    @Test
    @DisplayName("Should create a new restaurant successfully")
    void createRestaurant_shouldSaveRestaurantInDatabase() {
        // Arrange
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Test Restaurant");
        restaurantDTO.setAddress("123 Test Street");
        restaurantDTO.setDescription("A test restaurant");

        // Act
        Restaurant savedRestaurant = restaurantService.createRestaurant(restaurantDTO);

        // Assert
        assertNotNull(savedRestaurant);
        assertNotNull(savedRestaurant.getId());
        assertEquals("Test Restaurant", savedRestaurant.getName());
        assertEquals("123 Test Street", savedRestaurant.getAddress());
        assertEquals("A test restaurant", savedRestaurant.getDescription());
    }

    @Test
    @DisplayName("Should throw exception when creating restaurant with duplicate name")
    void createRestaurant_withDuplicateName_shouldThrowException() {
        // Arrange
        RestaurantDTO firstRestaurant = new RestaurantDTO();
        firstRestaurant.setName("Duplicate Restaurant");
        firstRestaurant.setAddress("123 First Street");
        firstRestaurant.setDescription("First restaurant");

        RestaurantDTO secondRestaurant = new RestaurantDTO();
        secondRestaurant.setName("Duplicate Restaurant");
        secondRestaurant.setAddress("456 Second Street");
        secondRestaurant.setDescription("Second restaurant");

        // Act
        restaurantService.createRestaurant(firstRestaurant);

        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            restaurantService.createRestaurant(secondRestaurant);
        });
    }

    @Test
    @DisplayName("Should get restaurant by ID successfully")
    void getRestaurantById_shouldReturnRestaurant() {
        // Arrange
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Find Me Restaurant");
        restaurantDTO.setAddress("789 Find Street");
        restaurantDTO.setDescription("Find this restaurant");

        Restaurant savedRestaurant = restaurantService.createRestaurant(restaurantDTO);

        // Act
        Restaurant foundRestaurant = restaurantService.getRestaurantById(savedRestaurant.getId());

        // Assert
        assertNotNull(foundRestaurant);
        assertEquals(savedRestaurant.getId(), foundRestaurant.getId());
        assertEquals("Find Me Restaurant", foundRestaurant.getName());
    }

    @Test
    @DisplayName("Should update restaurant successfully")
    void updateRestaurant_shouldUpdateRestaurantInDatabase() {
        // Arrange
        RestaurantDTO originalDTO = new RestaurantDTO();
        originalDTO.setName("Original Restaurant");
        originalDTO.setAddress("Original Address");
        originalDTO.setDescription("Original Description");

        Restaurant savedRestaurant = restaurantService.createRestaurant(originalDTO);

        RestaurantDTO updateDTO = new RestaurantDTO();
        updateDTO.setName("Updated Restaurant");
        updateDTO.setAddress("Updated Address");
        updateDTO.setDescription("Updated Description");

        // Act
        Restaurant updatedRestaurant = restaurantService.updateRestaurant(savedRestaurant.getId(), updateDTO);

        // Assert
        assertNotNull(updatedRestaurant);
        assertEquals(savedRestaurant.getId(), updatedRestaurant.getId());
        assertEquals("Updated Restaurant", updatedRestaurant.getName());
        assertEquals("Updated Address", updatedRestaurant.getAddress());
        assertEquals("Updated Description", updatedRestaurant.getDescription());
    }

    @Test
    @DisplayName("Should delete restaurant successfully")
    void deleteRestaurant_shouldRemoveRestaurantFromDatabase() {
        // Arrange
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Delete Me Restaurant");
        restaurantDTO.setAddress("Delete Street");
        restaurantDTO.setDescription("Delete this restaurant");

        Restaurant savedRestaurant = restaurantService.createRestaurant(restaurantDTO);

        // Act
        restaurantService.deleteRestaurant(savedRestaurant.getId());

        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            restaurantService.getRestaurantById(savedRestaurant.getId());
        });
    }

    @Test
    @DisplayName("Should search restaurants by name")
    void getRestaurants_withSearchTerm_shouldReturnFilteredResults() {
        // Arrange
        RestaurantDTO restaurant1 = new RestaurantDTO();
        restaurant1.setName("Pizza Palace");
        restaurant1.setAddress("Pizza Street");
        restaurant1.setDescription("Best pizza in town");

        RestaurantDTO restaurant2 = new RestaurantDTO();
        restaurant2.setName("Burger Joint");
        restaurant2.setAddress("Burger Avenue");
        restaurant2.setDescription("Great burgers");

        RestaurantDTO restaurant3 = new RestaurantDTO();
        restaurant3.setName("Pizza Corner");
        restaurant3.setAddress("Corner Street");
        restaurant3.setDescription("Another pizza place");

        restaurantService.createRestaurant(restaurant1);
        restaurantService.createRestaurant(restaurant2);
        restaurantService.createRestaurant(restaurant3);

        // Act
        List<Restaurant> pizzaRestaurants = restaurantService.getRestaurants("Pizza");

        // Assert
        assertEquals(2, pizzaRestaurants.size());
        assertEquals(2, pizzaRestaurants.stream()
                .mapToInt(r -> r.getName().toLowerCase().contains("pizza") ? 1 : 0)
                .sum());
    }
}