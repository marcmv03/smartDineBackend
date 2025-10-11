package com.smartDine.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartDine.dto.DishDTO;
import com.smartDine.dto.DrinkDTO;
import com.smartDine.dto.MenuItemDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Dish;
import com.smartDine.entity.Drink;
import com.smartDine.entity.MenuItem;
import com.smartDine.entity.Role;
import com.smartDine.entity.User;
import com.smartDine.repository.DishRepository;
import com.smartDine.repository.DrinkRepository;

@Service
public class MenuItemService {
    @Autowired
    private DrinkRepository drinkRepository;
    @Autowired
    private DishRepository dishRepository;
    @Autowired
    private RestaurantService restaurantService;

    public MenuItem createMenuItemForRestaurant(Long restaurantId, MenuItemDTO menuItemDTO, User user) {
        if (user == null || user.getRole() != Role.ROLE_BUSINESS || !(user instanceof Business business)) {
            throw new IllegalArgumentException("Only business users can create menu items");
        }
        if (!restaurantService.isOwnerOfRestaurant(restaurantId, business)) {
            throw new IllegalArgumentException("You do not own this restaurant");
        }
        MenuItem menuItem = createMenuItem(menuItemDTO);
        restaurantService.addMenuItem(restaurantId, menuItem);
        return menuItem;
    }

    public List<MenuItem> getMenuItemsByRestaurant(Long restaurantId) {
        return restaurantService.getMenuItems(restaurantId);
    }

    public MenuItem createMenuItem(MenuItemDTO menuItemDTO) {
        if (menuItemDTO instanceof DishDTO dishDTO) {
            return createDish(dishDTO);
        } else if (menuItemDTO instanceof DrinkDTO drinkDTO) {
            return createDrink(drinkDTO);
        }
        throw new IllegalArgumentException("Unsupported menu item type");
    }

    public Drink createDrink(DrinkDTO drinkDTO) {
        Drink drink = (Drink) new Drink()
            .setName(drinkDTO.getName())
            .setDescription(drinkDTO.getDescription())
            .setPrice(drinkDTO.getPrice());
        drink.setDrinkType(drinkDTO.getDrinkType());
        return drinkRepository.save(drink);
    }

    public Dish createDish(DishDTO menuItemDTO) {
        Dish dish = new Dish();
        dish.setName(menuItemDTO.getName());
        dish.setDescription(menuItemDTO.getDescription());
        dish.setPrice(menuItemDTO.getPrice());
        dish.setElements(menuItemDTO.getElements());
        dish.setCourseType(menuItemDTO.getCourseType());
        return dishRepository.save(dish);
    }
}
