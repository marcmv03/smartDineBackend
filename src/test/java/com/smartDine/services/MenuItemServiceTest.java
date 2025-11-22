package com.smartDine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.smartDine.adapters.ImageAdapter;
import com.smartDine.dto.DishDTO;
import com.smartDine.dto.DrinkDTO;
import com.smartDine.dto.UploadResponse;
import com.smartDine.entity.Business;
import com.smartDine.entity.CourseType;
import com.smartDine.entity.Dish;
import com.smartDine.entity.Drink;
import com.smartDine.entity.DrinkType;
import com.smartDine.entity.Element;
import com.smartDine.entity.MenuItem;
import com.smartDine.entity.Restaurant;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.RestaurantRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class MenuItemServiceTest {

    @Autowired
    private MenuItemService menuItemService;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @MockBean
    private ImageAdapter imageAdapter;

    private Business owner;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        owner = new Business("Menu Owner", "menuowner@test.com", "password", 900000001L);
        owner.setRestaurants(new ArrayList<>());
        owner = businessRepository.save(owner);

        restaurant = new Restaurant();
        restaurant.setName("Menu Owner Restaurant");
        restaurant.setAddress("123 Menu Street");
        restaurant.setDescription("Restaurant for menu tests");
        restaurant.setOwner(owner);
        restaurant.setMenu(new ArrayList<>());
        restaurant = restaurantRepository.save(restaurant);

        owner.getRestaurants().add(restaurant);
        businessRepository.save(owner);
    }

    @Test
    @DisplayName("Should create a dish and persist its attributes")
    void createDishTest() {
        DishDTO dishDTO = buildDishDTO("Vegan Burger", CourseType.MAIN_COURSE);
        dishDTO.setElements(List.of(Element.GLUTEN));

        Dish createdDish = menuItemService.createDish(dishDTO);

        assertNotNull(createdDish);
        assertEquals(dishDTO.getName(), createdDish.getName());
        assertEquals(dishDTO.getDescription(), createdDish.getDescription());
        assertEquals(dishDTO.getPrice(), createdDish.getPrice());
        assertEquals(dishDTO.getElements(), createdDish.getElements());
        assertEquals(dishDTO.getCourseType(), createdDish.getCourseType());
    }

    @Test
    @DisplayName("Should create a drink and persist its attributes")
    void createDrinkTest() {
        DrinkDTO drinkDTO = buildDrinkDTO("Cola");
        drinkDTO.setDrinkType(DrinkType.SOFT_DRINK);

        Drink createdDrink = menuItemService.createDrink(drinkDTO);

        assertNotNull(createdDrink);
        assertEquals(drinkDTO.getName(), createdDrink.getName());
        assertEquals(drinkDTO.getDescription(), createdDrink.getDescription());
        assertEquals(drinkDTO.getPrice(), createdDrink.getPrice());
        assertEquals(drinkDTO.getDrinkType(), createdDrink.getDrinkType());
    }

    @Test
    @DisplayName("Should add menu item to restaurant when business is the owner")
    void createMenuItemForRestaurantWithOwner() {
        DishDTO dishDTO = buildDishDTO("Carbonara", CourseType.MAIN_COURSE);
        dishDTO.setElements(List.of(Element.GLUTEN, Element.DAIRY));

        MenuItem created = menuItemService.createMenuItemForRestaurant(restaurant.getId(), dishDTO, owner);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(dishDTO.getName(), created.getName());

        List<MenuItem> menuItems = menuItemService.getMenuItemsByRestaurant(restaurant.getId());
        assertEquals(1, menuItems.size());
        assertEquals(created.getId(), menuItems.get(0).getId());
    }


    @Test
    @DisplayName("Should reject menu item creation when business does not own the restaurant")
    void createMenuItemForRestaurantWithDifferentOwner() {
        DishDTO dishDTO = buildDishDTO("Soup", CourseType.APPETIZER);

        Business otherBusiness = new Business("Other Owner", "otherowner@test.com", "password", 900000003L);
        otherBusiness.setRestaurants(new ArrayList<>());
        businessRepository.save(otherBusiness);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> menuItemService.createMenuItemForRestaurant(restaurant.getId(), dishDTO, otherBusiness)
        );

        assertEquals("You do not own this restaurant", exception.getMessage());
    }

    private DishDTO buildDishDTO(String name, CourseType courseType) {
        DishDTO dishDTO = new DishDTO();
        dishDTO.setName(name);
        dishDTO.setDescription("Test description for " + name);
        dishDTO.setPrice(12.99);
        dishDTO.setItemType("DISH");
        dishDTO.setCourseType(courseType);
        dishDTO.setElements(new ArrayList<>());
        return dishDTO;
    }

    private DrinkDTO buildDrinkDTO(String name) {
        DrinkDTO drinkDTO = new DrinkDTO();
        drinkDTO.setName(name);
        drinkDTO.setDescription("Test description for " + name);
        drinkDTO.setPrice(3.5);
        drinkDTO.setItemType("DRINK");
        return drinkDTO;
    }
    
    @Test
    @DisplayName("Should upload menu item image successfully when owner is valid")
    void testUploadMenuItemImage_Success() throws IOException {
        // Create a menu item first
        DishDTO dishDTO = buildDishDTO("Image Test Dish", CourseType.MAIN_COURSE);
        MenuItem menuItem = menuItemService.createMenuItemForRestaurant(restaurant.getId(), dishDTO, owner);
        
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "menu-item.jpg",
            "image/jpeg",
            "test menu item image".getBytes()
        );
        
        String expectedKeyPattern = "restaurants/" + restaurant.getId() + "/menu-items/" + menuItem.getId() + "/images/";
        String mockKey = expectedKeyPattern + "test-uuid.jpg";
        String mockUrl = "https://smartdine-s3-bucket.s3.amazonaws.com/" + mockKey;
        UploadResponse mockResponse = new UploadResponse(mockKey, mockUrl, "image/jpeg", file.getSize());
        
        // Mock the adapter response
        when(imageAdapter.uploadImage(any(), any())).thenReturn(mockResponse);
        
        // When
        UploadResponse response = menuItemService.uploadMenuItemImage(restaurant.getId(), menuItem.getId(), file, owner);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getKey());
        assertTrue(response.getKey().startsWith(expectedKeyPattern), "Key should start with expected pattern");
        assertTrue(response.getKey().endsWith(".jpg"), "Key should end with .jpg");
        assertNotNull(response.getUrl());
        assertEquals("image/jpeg", response.getContentType());
        assertEquals(file.getSize(), response.getSize());
        
        // Verify menu item entity was updated
        MenuItem updatedMenuItem = menuItemService.getMenuItemById(menuItem.getId());
        assertNotNull(updatedMenuItem.getImageUrl());
        assertTrue(updatedMenuItem.getImageUrl().startsWith(expectedKeyPattern));
    }
    
    @Test
    @DisplayName("Should throw exception when uploading image for non-existent menu item")
    void testUploadMenuItemImage_MenuItemNotFound() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "menu-item.jpg",
            "image/jpeg",
            "test menu item image".getBytes()
        );
        
        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> menuItemService.uploadMenuItemImage(restaurant.getId(), 9L, file, owner)
        );
        
        assertEquals("Menu item not found with ID: 9", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when user does not own the restaurant")
    void testUploadMenuItemImage_NotOwner() throws IOException {
        // Create a menu item
        DishDTO dishDTO = buildDishDTO("Unauthorized Dish", CourseType.APPETIZER);
        MenuItem menuItem = menuItemService.createMenuItemForRestaurant(restaurant.getId(), dishDTO, owner);
        
        // Create another business owner
        Business otherOwner = new Business("Other Owner", "other@example.com", "password", 888888888L);
        businessRepository.save(otherOwner);
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "menu-item.jpg",
            "image/jpeg",
            "test menu item image".getBytes()
        );
        
        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> menuItemService.uploadMenuItemImage(restaurant.getId(), menuItem.getId(), file, otherOwner)
        );
        
        assertEquals("You do not own this restaurant", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when file is empty")
    void testUploadMenuItemImage_EmptyFile() throws IOException {
        // Create a menu item
        DishDTO dishDTO = buildDishDTO("Empty File Dish", CourseType.DESSERT);
        MenuItem menuItem = menuItemService.createMenuItemForRestaurant(restaurant.getId(), dishDTO, owner);
        
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );
        
        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> menuItemService.uploadMenuItemImage(restaurant.getId(), menuItem.getId(), emptyFile, owner)
        );
        
        assertEquals("File cannot be null or empty", exception.getMessage());
    }
}
