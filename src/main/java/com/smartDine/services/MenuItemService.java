package com.smartDine.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.smartDine.adapters.ImageAdapter;
import com.smartDine.dto.DishDTO;
import com.smartDine.dto.DrinkDTO;
import com.smartDine.dto.MenuItemDTO;
import com.smartDine.dto.UploadResponse;
import com.smartDine.entity.Business;
import com.smartDine.entity.Dish;
import com.smartDine.entity.Drink;
import com.smartDine.entity.MenuItem;
import com.smartDine.entity.Restaurant;
import com.smartDine.repository.DishRepository;
import com.smartDine.repository.DrinkRepository;
import com.smartDine.repository.MenuItemRepository;

@Service
public class MenuItemService {
    @Autowired
    private  MenuItemRepository menuItemRepository;
    @Autowired
    private DrinkRepository drinkRepository;
    @Autowired
    private DishRepository dishRepository;
    @Autowired
    private RestaurantService restaurantService;
    @Autowired
    private ImageAdapter imageAdapter;
 

    public MenuItem createMenuItemForRestaurant(Long restaurantId, MenuItemDTO menuItemDTO, Business owner) {
        if (!restaurantService.isOwnerOfRestaurant(restaurantId, owner)) {
            throw new IllegalArgumentException("You do not own this restaurant");
        }
        MenuItem menuItem = createMenuItem(menuItemDTO,restaurantId);
        menuItem.setRestaurant(restaurantService.getRestaurantById(restaurantId));
        restaurantService.addMenuItem(restaurantId, menuItem);
        return menuItem;
    }

    public List<MenuItem> getMenuItemsByRestaurant(Long restaurantId) {
        return restaurantService.getMenuItems(restaurantId);
    }

    public MenuItem createMenuItem(MenuItemDTO menuItemDTO,Long restaurantId) {
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        if (menuItemDTO instanceof DishDTO dishDTO) {
            Dish dish =  createDish(dishDTO);
            dish.setRestaurant(restaurant);
            return dish ;
        } else if (menuItemDTO instanceof DrinkDTO drinkDTO) {
            Drink drink = createDrink(drinkDTO);
            drink.setRestaurant(restaurant);
            return drink ;
        }
        throw new IllegalArgumentException("Unsupported menu item type");
    }

    public Drink createDrink(DrinkDTO drinkDTO) {
        Drink drink = DrinkDTO.toEntity(drinkDTO);
        return drinkRepository.save(drink);
    }

    public Dish createDish(DishDTO dishDTO) {
        Dish dish = DishDTO.toEntity(dishDTO);
        return dishRepository.save(dish);
    }
    public MenuItem getMenuItemById(Long menuItemId) {
        return menuItemRepository.findById(menuItemId).get() ;
}
public void addImage(Long menuItemId, String imageUrl) {
    MenuItem menuItem = menuItemRepository.findById(menuItemId)
        .orElseThrow(() -> new IllegalArgumentException("Menu item not found with ID: " + menuItemId));
    menuItem.setImageUrl(imageUrl);
    menuItemRepository.save(menuItem);
    
}

    /**
     * Uploads an image for a MenuItem and assigns it to the entity.
     * Validates that the business user owns the restaurant containing the menu item.
     * 
     * @param restaurantId the ID of the restaurant
     * @param menuItemId the ID of the menu item
     * @param file the image file to upload
     * @param business the business owner
     * @return UploadResponse containing upload details
     * @throws IOException if the upload fails
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public UploadResponse uploadMenuItemImage(Long restaurantId, Long menuItemId, MultipartFile file, Business business) throws IOException {
        
        // Validate restaurant ownership
        if (!restaurantService.isOwnerOfRestaurant(restaurantId, business)) {
            throw new IllegalArgumentException("You do not own this restaurant");
        }
        
        // Validate menu item exists
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new IllegalArgumentException("Menu item not found with ID: " + menuItemId));
        
        // Validate menu item belongs to the restaurant
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        if (!restaurant.getMenu().contains(menuItem)) {
            throw new IllegalArgumentException("Menu item does not belong to this restaurant");
        }
        
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        // Extract file extension
        String ext = Optional.ofNullable(file.getOriginalFilename())
            .filter(n -> n.contains("."))
            .map(n -> n.substring(n.lastIndexOf('.') + 1))
            .orElse("jpg");
        
        // Generate S3 key/path
        String keyName = "restaurants/%d/menu-items/%d/images/%s.%s"
            .formatted(restaurantId, menuItemId, java.util.UUID.randomUUID(), ext);
        
        // Upload image using the adapter
        UploadResponse response = imageAdapter.uploadImage(file, keyName);
        
        // Assign the image to the menu item
        menuItem.setImageUrl(keyName);
        menuItemRepository.save(menuItem);
        
        return response;
    }
    
    @Transactional
    public void deleteMenuItem(Long restaurantId, Long menuItemId, Business business) {
        if (business == null || business.getId() == null) {
            throw new IllegalArgumentException("Business owner is required to delete a menu item");
        }
        
        // Validate restaurant ownership
        if (!restaurantService.isOwnerOfRestaurant(restaurantId, business)) {
            throw new IllegalArgumentException("Business is not the owner of the restaurant");
        }
        
        // Validate menu item exists
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new IllegalArgumentException("Menu item not found with ID: " + menuItemId));
        
        // Delete the menu item
        menuItemRepository.delete(menuItem);
        menuItemRepository.flush() ; 
        imageAdapter.deleteImage(menuItem.getImageUrl());
    }

}
