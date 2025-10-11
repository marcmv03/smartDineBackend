package com.smartDine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.smartDine.dto.DishDTO;
import com.smartDine.dto.DrinkDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.CourseType;
import com.smartDine.entity.Customer;
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
@Transactional
class MenuItemServiceTest {

    @Autowired
    private MenuItemService menuItemService;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

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
    @DisplayName("Should reject menu item creation when user is not a business")
    void createMenuItemForRestaurantWithNonBusinessUser() {
        DishDTO dishDTO = buildDishDTO("Salad", CourseType.APPETIZER);
        Customer customer = new Customer("Menu Customer", "menucustomer@test.com", "password", 900000002L);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> menuItemService.createMenuItemForRestaurant(restaurant.getId(), dishDTO, customer)
        );

        assertEquals("Only business users can create menu items", exception.getMessage());
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
}
