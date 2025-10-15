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
    public ResponseEntity<List<RestaurantDTO>> getRestaurants(
        @RequestParam(value = "search", required = false) String searchTerm) {
        List<Restaurant> restaurants = restaurantService.getRestaurants(searchTerm);
        List<RestaurantDTO> restaurantDTOs = RestaurantDTO.fromEntity(restaurants);
        return ResponseEntity.ok(restaurantDTOs);
    }
    
    /**
     * GET /restaurants/{id} - Get restaurant by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDTO> getRestaurantById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        RestaurantDTO restaurantDTO = RestaurantDTO.fromEntity(restaurant);
        return ResponseEntity.ok(restaurantDTO);
    }
    
    /**
     * POST /restaurants - Create a new restaurant
     */
    @PostMapping
    public ResponseEntity<RestaurantDTO> createRestaurant(@Valid @RequestBody RestaurantDTO restaurantDTO, @AuthenticationPrincipal User user) {
        if (!(user instanceof Business)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Restaurant createdRestaurant = restaurantService.createRestaurant(restaurantDTO, (Business) user);
        RestaurantDTO createdRestaurantDTO = RestaurantDTO.fromEntity(createdRestaurant);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRestaurantDTO);
    }
    
    /**
     * PUT /restaurants/{id} - Update an existing restaurant
     */
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDTO> updateRestaurant(
        @PathVariable Long id, 
        @Valid @RequestBody RestaurantDTO restaurantDTO) {
        Restaurant updatedRestaurant = restaurantService.updateRestaurant(id, restaurantDTO);
        RestaurantDTO updatedRestaurantDTO = RestaurantDTO.fromEntity(updatedRestaurant);
        return ResponseEntity.ok(updatedRestaurantDTO);
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
