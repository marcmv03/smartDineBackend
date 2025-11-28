package com.smartDine.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

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
import com.smartDine.repository.DishRepository;
import com.smartDine.repository.DrinkRepository;
import com.smartDine.repository.MenuItemRepository;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private DrinkRepository drinkRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ImageAdapter imageAdapter;

    @InjectMocks
    private MenuItemService menuItemService;

    private Business owner;
    private Restaurant restaurant;
    private Dish dish;
    private Drink drink;

    @BeforeEach
    void setUp() {
        owner = new Business("Menu Owner", "menuowner@test.com", "password", 900000001L);
        owner.setId(1L);
        owner.setRestaurants(new ArrayList<>());

        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Menu Owner Restaurant");
        restaurant.setAddress("123 Menu Street");
        restaurant.setDescription("Restaurant for menu tests");
        restaurant.setOwner(owner);
        restaurant.setMenu(new ArrayList<>());

        owner.getRestaurants().add(restaurant);

        dish = new Dish();
        dish.setId(1L);
        dish.setName("Vegan Burger");
        dish.setDescription("Test description for Vegan Burger");
        dish.setPrice(12.99);
        dish.setCourseType(CourseType.MAIN_COURSE);
        dish.setElements(List.of(Element.GLUTEN));
        dish.setRestaurant(restaurant);

        drink = new Drink();
        drink.setId(2L);
        drink.setName("Cola");
        drink.setDescription("Test description for Cola");
        drink.setPrice(3.5);
        drink.setDrinkType(DrinkType.SOFT_DRINK);
        drink.setRestaurant(restaurant);
    }

    @Test
    @DisplayName("Should create a dish and persist its attributes")
    void createDishTest() {
        DishDTO dishDTO = buildDishDTO("Vegan Burger", CourseType.MAIN_COURSE);
        dishDTO.setElements(List.of(Element.GLUTEN));

        when(dishRepository.save(any(Dish.class))).thenReturn(dish);

        Dish createdDish = menuItemService.createDish(dishDTO);

        assertNotNull(createdDish);
        assertEquals(dish.getName(), createdDish.getName());
        assertEquals(dish.getDescription(), createdDish.getDescription());
        assertEquals(dish.getPrice(), createdDish.getPrice());
        assertEquals(dish.getElements(), createdDish.getElements());
        assertEquals(dish.getCourseType(), createdDish.getCourseType());
        verify(dishRepository).save(any(Dish.class));
    }

    @Test
    @DisplayName("Should create a drink and persist its attributes")
    void createDrinkTest() {
        DrinkDTO drinkDTO = buildDrinkDTO("Cola");
        drinkDTO.setDrinkType(DrinkType.SOFT_DRINK);

        when(drinkRepository.save(any(Drink.class))).thenReturn(drink);

        Drink createdDrink = menuItemService.createDrink(drinkDTO);

        assertNotNull(createdDrink);
        assertEquals(drink.getName(), createdDrink.getName());
        assertEquals(drink.getDescription(), createdDrink.getDescription());
        assertEquals(drink.getPrice(), createdDrink.getPrice());
        assertEquals(drink.getDrinkType(), createdDrink.getDrinkType());
        verify(drinkRepository).save(any(Drink.class));
    }

    @Test
    @DisplayName("Should add menu item to restaurant when business is the owner")
    void createMenuItemForRestaurantWithOwner() {
        DishDTO dishDTO = buildDishDTO("Carbonara", CourseType.MAIN_COURSE);
        dishDTO.setElements(List.of(Element.GLUTEN, Element.DAIRY));

        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(dishRepository.save(any(Dish.class))).thenReturn(dish);
        when(restaurantService.addMenuItem(any(), any())).thenReturn(true);

        MenuItem created = menuItemService.createMenuItemForRestaurant(restaurant.getId(), dishDTO, owner);

        assertNotNull(created);
        assertEquals(dish.getName(), created.getName());
        verify(dishRepository).save(any(Dish.class));
        verify(restaurantService).addMenuItem(any(), any());
    }

    @Test
    @DisplayName("Should reject menu item creation when business does not own the restaurant")
    void createMenuItemForRestaurantWithDifferentOwner() {
        DishDTO dishDTO = buildDishDTO("Soup", CourseType.APPETIZER);

        Business otherBusiness = new Business("Other Owner", "otherowner@test.com", "password", 900000003L);
        otherBusiness.setId(2L);
        otherBusiness.setRestaurants(new ArrayList<>());

        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), otherBusiness)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> menuItemService.createMenuItemForRestaurant(restaurant.getId(), dishDTO, otherBusiness)
        );

        assertEquals("You do not own this restaurant", exception.getMessage());
        verify(dishRepository, never()).save(any(Dish.class));
    }

    @Test
    @DisplayName("Should upload menu item image successfully when owner is valid")
    void testUploadMenuItemImage_Success() throws IOException {
        restaurant.getMenu().add(dish);
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "menu-item.jpg",
            "image/jpeg",
            "test menu item image".getBytes()
        );
        
        String expectedKeyPattern = "restaurants/" + restaurant.getId() + "/menu-items/" + dish.getId() + "/images/";
        String mockKey = expectedKeyPattern + "test-uuid.jpg";
        String mockUrl = "https://smartdine-s3-bucket.s3.amazonaws.com/" + mockKey;
        UploadResponse mockResponse = new UploadResponse(mockKey, mockUrl, "image/jpeg", file.getSize());
        
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(menuItemRepository.findById(dish.getId())).thenReturn(Optional.of(dish));
        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(imageAdapter.uploadImage(any(), any())).thenReturn(mockResponse);
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(dish);
        
        UploadResponse response = menuItemService.uploadMenuItemImage(restaurant.getId(), dish.getId(), file, owner);
        
        assertNotNull(response);
        assertNotNull(response.getKey());
        verify(menuItemRepository).save(any(MenuItem.class));
        verify(imageAdapter).uploadImage(any(), any());
    }
    
    @Test
    @DisplayName("Should throw exception when uploading image for non-existent menu item")
    void testUploadMenuItemImage_MenuItemNotFound() throws IOException {
        Long nonExistentId = 9L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "menu-item.jpg",
            "image/jpeg",
            "test menu item image".getBytes()
        );
        
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(menuItemRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> menuItemService.uploadMenuItemImage(restaurant.getId(), nonExistentId, file, owner)
        );
        
        assertEquals("Menu item not found with ID: " + nonExistentId, exception.getMessage());
        verify(imageAdapter, never()).uploadImage(any(), any());
    }
    
    @Test
    @DisplayName("Should throw exception when user does not own the restaurant")
    void testUploadMenuItemImage_NotOwner() throws IOException {
        Business otherOwner = new Business("Other Owner", "other@example.com", "password", 888888888L);
        otherOwner.setId(2L);
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "menu-item.jpg",
            "image/jpeg",
            "test menu item image".getBytes()
        );
        
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), otherOwner)).thenReturn(false);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> menuItemService.uploadMenuItemImage(restaurant.getId(), dish.getId(), file, otherOwner)
        );
        
        assertEquals("You do not own this restaurant", exception.getMessage());
        verify(imageAdapter, never()).uploadImage(any(), any());
    }
    
    @Test
    @DisplayName("Should throw exception when file is empty")
    void testUploadMenuItemImage_EmptyFile() throws IOException {
        restaurant.getMenu().add(dish);
        
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );
        
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(menuItemRepository.findById(dish.getId())).thenReturn(Optional.of(dish));
        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> menuItemService.uploadMenuItemImage(restaurant.getId(), dish.getId(), emptyFile, owner)
        );
        
        assertEquals("File cannot be null or empty", exception.getMessage());
        verify(imageAdapter, never()).uploadImage(any(), any());
    }

    @Test
    @DisplayName("Should delete menu item successfully")
    void deleteMenuItemSuccess() {
        dish.setImageUrl("test-image-url.jpg");
        
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(menuItemRepository.findById(dish.getId())).thenReturn(Optional.of(dish));
        doNothing().when(menuItemRepository).delete(dish);
        doNothing().when(menuItemRepository).flush();
        doNothing().when(imageAdapter).deleteImage("test-image-url.jpg");

        menuItemService.deleteMenuItem(restaurant.getId(), dish.getId(), owner);

        verify(menuItemRepository).delete(dish);
        verify(menuItemRepository).flush();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent menu item")
    void deleteNonExistentMenuItem() {
        Long nonExistentId = 999L;

        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(menuItemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> menuItemService.deleteMenuItem(restaurant.getId(), nonExistentId, owner)
        );

        assertEquals("Menu item not found with ID: " + nonExistentId, exception.getMessage());
        verify(menuItemRepository, never()).delete(any(MenuItem.class));
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
}
