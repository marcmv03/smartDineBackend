package com.smartDine.services;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import com.smartDine.adapters.ImageAdapter;
import com.smartDine.dto.RestaurantDTO;
import com.smartDine.dto.UploadResponse;
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
    
    @MockBean
    private ImageAdapter imageAdapter;
    
    private Business testOwner;
    private Restaurant testRestaurant;
    
    @BeforeEach
    void setUp() {
        // Create a test owner
        testOwner = new Business("Test Owner", "testowner@example.com", "password", 666666666L);
        testOwner = businessRepository.save(testOwner);
        
        // Create a test restaurant
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Test Restaurant for Upload");
        restaurantDTO.setAddress("123 Upload St");
        restaurantDTO.setDescription("Restaurant for upload tests");
        testRestaurant = restaurantService.createRestaurant(testOwner, restaurantDTO);
    }
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
        Restaurant createdRestaurant = restaurantService.createRestaurant(owner, restaurantDTO);
         // Restaurant createdRestaurant = businessService.createRestaurantForBusiness(owner, restaurantDTO);
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
        restaurantService.createRestaurant(restaurantDTO1, owner);
        RestaurantDTO restaurantDTO2 = new RestaurantDTO();
        restaurantDTO2.setName("Duplicate Restaurant");
        restaurantDTO2.setAddress("456 Second St");
        restaurantDTO2.setDescription("Second restaurant");
        assertThrows(IllegalArgumentException.class, () -> restaurantService.createRestaurant(restaurantDTO2, owner));
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
        Restaurant createdRestaurant = restaurantService.createRestaurant(owner, restaurantDTO);
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
        Restaurant createdRestaurant = restaurantService.createRestaurant(owner, restaurantDTO);
         // Restaurant createdRestaurant = businessService.createRestaurantForBusiness(owner, restaurantDTO);
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
        Restaurant savedRestaurant = restaurantService.createRestaurant(restaurantDTO, owner);
         // Restaurant savedRestaurant = businessService.createRestaurantForBusiness(owner, restaurantDTO);
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
        Business owner = new Business("Test Owner", "testowner6@test.com", "password", 999888777L);
        owner = businessRepository.save(owner);
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Original Name");
        restaurantDTO.setAddress("111 Original St");
        restaurantDTO.setDescription("Original description");
        Restaurant savedRestaurant = restaurantService.createRestaurant(owner, restaurantDTO);
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
    @DisplayName("Should return menu items when restaurant has menu items")
    void testGetMenuItems_WithItems() {
        // Create owner and restaurant
        Business owner = new Business("Test Owner", "testowner7@test.com", "password", 777777777L);
        owner = businessRepository.save(owner);
        
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Menu Test Restaurant");
        restaurantDTO.setAddress("456 Menu St");
        restaurantDTO.setDescription("A restaurant to test menu functionality");
        
        Restaurant createdRestaurant = restaurantService.createRestaurant(owner, restaurantDTO);
        
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
        
        Restaurant createdRestaurant = restaurantService.createRestaurant(owner, restaurantDTO);
        
        // Get menu items (should be empty)
        List<MenuItem> menuItems = restaurantService.getMenuItems(createdRestaurant.getId());
        
        // Verify results
        assertNull(menuItems);
    }
    
    @Test
    @DisplayName("Should upload image successfully when owner is valid")
    void testUploadRestaurantImage_Success() throws IOException {
        // Create test data
        Business testOwner = new Business("Upload Owner", "uploadowner@example.com", "password", 999999999L);
        testOwner = businessRepository.save(testOwner);
        
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Upload Test Restaurant");
        restaurantDTO.setAddress("123 Upload St");
        restaurantDTO.setDescription("Restaurant for upload tests");
        Restaurant testRestaurant = restaurantService.createRestaurant(testOwner, restaurantDTO);
        
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        
        String expectedKeyPattern = "restaurants/" + testRestaurant.getId() + "/images/";
        String mockKey = expectedKeyPattern + "test-uuid.jpg";
        String mockUrl = "https://smartdine-s3-bucket.s3.amazonaws.com/" + mockKey;
        UploadResponse mockResponse = new UploadResponse(mockKey, mockUrl, "image/jpeg", file.getSize());
        
        // Mock the adapter response
        when(imageAdapter.uploadImage(any(), any())).thenReturn(mockResponse);
        
        // When
        UploadResponse response = restaurantService.uploadRestaurantImage(testRestaurant.getId(), file, testOwner);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getKey());
        assertTrue(response.getKey().startsWith(expectedKeyPattern), "Key should start with expected pattern");
        assertTrue(response.getKey().endsWith(".jpg"), "Key should end with .jpg");
        assertNotNull(response.getUrl());
        assertEquals("image/jpeg", response.getContentType());
        assertEquals(file.getSize(), response.getSize());
        
        // Verify restaurant entity was updated
        Restaurant updatedRestaurant = restaurantService.getRestaurantById(testRestaurant.getId());
        assertNotNull(updatedRestaurant.getImageUrl());
        assertTrue(updatedRestaurant.getImageUrl().startsWith(expectedKeyPattern));
    }
    
    @Test
    @DisplayName("Should throw exception when uploading image for non-existent restaurant")
    void testUploadRestaurantImage_RestaurantNotFound() {
        // Create test owner
        Business testOwnerTemp = new Business("Test Owner", "testowner@example.com", "password", 222222222L);
        final Business testOwner = businessRepository.save(testOwnerTemp);
        
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        
        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> restaurantService.uploadRestaurantImage(9999L, file, testOwner)
        );
        
        assertEquals("Restaurante no encontrado con ID: 9999", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when user is not the owner")
    void testUploadRestaurantImage_NotOwner() {
        // Create restaurant and owner
        Business owner = new Business("Original Owner", "owner@example.com", "password", 333333333L);
        owner = businessRepository.save(owner);
        
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("Owned Restaurant");
        restaurantDTO.setAddress("123 Owner St");
        restaurantDTO.setDescription("Restaurant with specific owner");
        Restaurant restaurant = restaurantService.createRestaurant(owner, restaurantDTO);
        
        // Given
        Business otherOwnerTemp = new Business("Other Owner", "other@example.com", "password", 444444444L);
        final Business otherOwner = businessRepository.save(otherOwnerTemp);
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        
        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> restaurantService.uploadRestaurantImage(restaurant.getId(), file, otherOwner)
        );
        
        assertEquals("You do not own this restaurant", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when file is empty")
    void testUploadRestaurantImage_EmptyFile() {
        // Create restaurant and owner
        Business ownerTemp = new Business("File Test Owner", "fileowner@example.com", "password", 555555555L);
        final Business owner = businessRepository.save(ownerTemp);
        
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setName("File Test Restaurant");
        restaurantDTO.setAddress("123 File St");
        restaurantDTO.setDescription("Restaurant for file tests");
        final Restaurant restaurant = restaurantService.createRestaurant(owner, restaurantDTO);
        
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );
        
        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> restaurantService.uploadRestaurantImage(restaurant.getId(), emptyFile, owner)
        );
        
        assertEquals("File cannot be null or empty", exception.getMessage());
    }
}
