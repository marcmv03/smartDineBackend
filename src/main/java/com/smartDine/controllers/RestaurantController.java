package com.smartDine.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.RestaurantDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.Role;
import com.smartDine.entity.User;
import com.smartDine.services.RestaurantService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/smartdine/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {
    @Autowired
    private RestaurantService restaurantService;
    
    /**
     * GET /restaurants - Get all restaurants or search by name
     */
    @GetMapping
    public ResponseEntity<List<Restaurant>> getRestaurants(
        @RequestParam(value = "search", required = false) String searchTerm) {
        List<Restaurant> restaurants = restaurantService.getRestaurants(searchTerm);
        return ResponseEntity.ok(restaurants);
    }
    
    /**
     * GET /restaurants/{id} - Get restaurant by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id ,@AuthenticationPrincipal User user ) {
        if (user.getRole() != Role.ROLE_ADMIN && user.getRole() != Role.ROLE_BUSINESS) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        List<Restaurant> userRestaurants = ((Business) user).getRestaurants();
        userRestaurants.add(restaurant) ; 
        ((Business) user).setRestaurants(userRestaurants);
        return ResponseEntity.ok(restaurant);
    }
    
    /**
     * POST /restaurants - Create a new restaurant
     */
    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(@Valid @RequestBody RestaurantDTO restaurantDTO) {
        Restaurant createdRestaurant = restaurantService.createRestaurant(restaurantDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRestaurant);
    }
    
    /**
     * PUT /restaurants/{id} - Update an existing restaurant
     */
    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(
        @PathVariable Long id, 
        @Valid @RequestBody RestaurantDTO restaurantDTO) {
        Restaurant updatedRestaurant = restaurantService.updateRestaurant(id, restaurantDTO);
        return ResponseEntity.ok(updatedRestaurant);
    }
    
    /**
     * DELETE /restaurants/{id} - Delete a restaurant
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
}
