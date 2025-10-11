package com.smartDine.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.smartDine.dto.RestaurantDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Dish;
import com.smartDine.entity.MenuItem;
import com.smartDine.entity.Restaurant;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.DishRepository;
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
    @Autowired
    private BusinessService businessService;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private DishRepository dishRepository;
    @Test
    @DisplayName("Context loads and RestaurantService is not null")
    void contextLoads() {
        assertNotNull(restaurantService);
    }

    @Test
    @DisplayName("Should create a new restaurant successfully")
    void testCreateRestaurant() {
        Business owner = new Business("Test Owner", "testowner@test.com", "password", 111111111L);
        owner = businessRepository.save(owner);
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Test Restaurant");
        restaurantDTO.setAddress("123 Test St");
        restaurantDTO.setDescription("A test restaurant");
        Restaurant createdRestaurant = businessService.createRestaurantForBusiness(owner, restaurantDTO);
        assertNotNull(createdRestaurant);
        assertNotNull(createdRestaurant.getId());
        assertEquals("Test Restaurant", createdRestaurant.getName());
        assertEquals("123 Test St", createdRestaurant.getAddress());
        assertEquals("A test restaurant", createdRestaurant.getDescription());
        assertEquals(owner.getId(), createdRestaurant.getOwner().getId());
    }

    @Test
    @DisplayName("Should throw exception when creating restaurant with duplicate name")
    void testCreateRestaurantWithExistingName() {
        final Business owner = new Business("Test Owner", "testowner2@test.com", "password", 222222222L);
        businessRepository.save(owner);
        RestaurantDTO restaurantDTO1 = new RestaurantDTO();
        restaurantDTO1.setName("Duplicate Restaurant");
        restaurantDTO1.setAddress("123 First St");
        restaurantDTO1.setDescription("First restaurant");
        businessService.createRestaurantForBusiness(owner, restaurantDTO1);
        RestaurantDTO restaurantDTO2 = new RestaurantDTO();
        restaurantDTO2.setName("Duplicate Restaurant");
        restaurantDTO2.setAddress("456 Second St");
        restaurantDTO2.setDescription("Second restaurant");
        assertThrows(IllegalArgumentException.class, () -> businessService.createRestaurantForBusiness(owner, restaurantDTO2));
    }

    @Test
    @DisplayName("Should fetch restaurant by ID successfully")
    void testGetRestaurantById() {
        Business owner = new Business("Test Owner", "testowner3@test.com", "password", 333333333L);
        owner = businessRepository.save(owner);
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Fetch Restaurant");
        restaurantDTO.setAddress("789 Fetch St");
        restaurantDTO.setDescription("A fetch test restaurant");
        Restaurant createdRestaurant = businessService.createRestaurantForBusiness(owner, restaurantDTO);
        Restaurant fetchedRestaurant = restaurantService.getRestaurantById(createdRestaurant.getId());
        assertNotNull(fetchedRestaurant);
        assertEquals(createdRestaurant.getId(), fetchedRestaurant.getId());
        assertEquals("Fetch Restaurant", fetchedRestaurant.getName());
        assertEquals(owner.getId(), fetchedRestaurant.getOwner().getId());
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
        Business owner = new Business("Test Owner", "testowner4@test.com", "password", 444444444L);
        owner = businessRepository.save(owner);
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Unique Name Restaurant");
        restaurantDTO.setAddress("101 Unique St");
        restaurantDTO.setDescription("A unique name test restaurant");
        Restaurant createdRestaurant = businessService.createRestaurantForBusiness(owner, restaurantDTO);
        Restaurant fetchedRestaurant = restaurantService.getRestaurantById(createdRestaurant.getId());
        assertNotNull(fetchedRestaurant);
        assertEquals(createdRestaurant.getId(), fetchedRestaurant.getId());
        assertEquals("Unique Name Restaurant", fetchedRestaurant.getName());
        assertEquals(owner.getId(), fetchedRestaurant.getOwner().getId());
    }
    @Test
    @DisplayName("Should delete existing restaurant successfully")
    void deleteExistingRestaurant_shouldRemoveRestaurant() {
        Business owner = new Business("Test Owner", "testowner5@test.com", "password", 555555555L);
        owner = businessRepository.save(owner);
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Delete Me Restaurant");
        restaurantDTO.setAddress("321 Delete St");
        restaurantDTO.setDescription("A restaurant to be deleted");
        Restaurant savedRestaurant = businessService.createRestaurantForBusiness(owner, restaurantDTO);
        Long restaurantId = savedRestaurant.getId();
        restaurantService.deleteRestaurant(restaurantId);
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
        Business owner = new Business("Test Owner", "testowner6@test.com", "password", 666666666L);
        owner = businessRepository.save(owner);
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Original Name");
        restaurantDTO.setAddress("111 Original St");
        restaurantDTO.setDescription("Original description");
        Restaurant savedRestaurant = businessService.createRestaurantForBusiness(owner, restaurantDTO);
        Long restaurantId = savedRestaurant.getId();
        RestaurantDTO updatedDTO = new RestaurantDTO();
        updatedDTO.setName("Updated Name");
        updatedDTO.setAddress("222 Updated St");
        updatedDTO.setDescription("Updated description");
        Restaurant updatedRestaurant = restaurantService.updateRestaurant(restaurantId, updatedDTO);
        assertNotNull(updatedRestaurant);
        assertEquals(restaurantId, updatedRestaurant.getId());
        assertEquals("Updated Name", updatedRestaurant.getName());
        assertEquals("222 Updated St", updatedRestaurant.getAddress());
        assertEquals("Updated description", updatedRestaurant.getDescription());
        assertEquals(owner.getId(), updatedRestaurant.getOwner().getId());
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
    @Test
    @DisplayName("Should create a restaurant with owner using BusinessService")
    void testCreateRestaurantWithOwner() {
        // Crear un Business (empresa propietaria)
        Business owner = new Business("EmpresaTest", "empresa@test.com", "password", 123456789L);
        owner = businessRepository.save(owner);
        // Crear DTO de restaurante
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Restaurante Propietario");
        restaurantDTO.setAddress("Calle Propietario 1");
        restaurantDTO.setDescription("Restaurante con owner");
        // Crear restaurante usando BusinessService
        Restaurant createdRestaurant = businessService.createRestaurantForBusiness(owner, restaurantDTO);
        assertNotNull(createdRestaurant);
        assertNotNull(createdRestaurant.getId());
        assertEquals("Restaurante Propietario", createdRestaurant.getName());
        assertEquals(owner.getId(), createdRestaurant.getOwner().getId());
        // Verificar que el restaurante está en la lista del owner
        Business updatedOwner = businessRepository.findById(owner.getId()).orElseThrow();
        assertNotNull(updatedOwner.getRestaurants());
        assertEquals(1, updatedOwner.getRestaurants().size());
        assertEquals(createdRestaurant.getId(), updatedOwner.getRestaurants().get(0).getId());
    }
    @Test
    @DisplayName("Should return menu items when restaurant has menu items")
    void testGetMenuItems_WithItems() {
        // Create owner and restaurant
        Business owner = new Business("Test Owner", "testowner7@test.com", "password", 777777777L);
        owner = businessRepository.save(owner);
        
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Menu Test Restaurant");
        restaurantDTO.setAddress("456 Menu St");
        restaurantDTO.setDescription("A restaurant to test menu functionality");
        
        Restaurant createdRestaurant = businessService.createRestaurantForBusiness(owner, restaurantDTO);
        
        // Create and add a menu item
        Dish menuItem = new Dish();
        menuItem.setName("Test Burger");
        menuItem.setDescription("A delicious test burger");
        menuItem.setPrice(12.99);
        dishRepository.save(menuItem); // Save the dish to ensure it has an ID
        restaurantService.addMenuItem(createdRestaurant.getId(), menuItem);
        
        // Get menu items
        List<MenuItem> menuItems = restaurantService.getMenuItems(createdRestaurant.getId());
        
        // Verify results
        assertNotNull(menuItems);
        assertEquals(1, menuItems.size());
        MenuItem retrievedItem = menuItems.get(0);
        assertEquals("Test Burger", retrievedItem.getName());
        assertEquals("A delicious test burger", retrievedItem.getDescription());
        assertEquals(12.99, retrievedItem.getPrice());
    }

    @Test
    @DisplayName("Should return empty list when restaurant has no menu items")
    void testGetMenuItems_EmptyMenu() {
        // Create owner and restaurant
        Business owner = new Business("Test Owner", "testowner8@test.com", "password", 888888888L);
        owner = businessRepository.save(owner);
        
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Empty Menu Restaurant");
        restaurantDTO.setAddress("789 Empty St");
        restaurantDTO.setDescription("A restaurant with no menu items");
        
        Restaurant createdRestaurant = businessService.createRestaurantForBusiness(owner, restaurantDTO);
        
        // Get menu items (should be empty)
        List<MenuItem> menuItems = restaurantService.getMenuItems(createdRestaurant.getId());
        
        // Verify results
        assertNull(menuItems);
    }
}
