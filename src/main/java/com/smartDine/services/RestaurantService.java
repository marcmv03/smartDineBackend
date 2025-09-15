package com.smartDine.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartDine.dto.RestaurantDTO;
import com.smartDine.entity.Restaurant;
import com.smartDine.repository.RestaurantRepository;

@Service 
public class RestaurantService {
    @Autowired 
    private RestaurantRepository restaurantRepository;
    
    /**
     * Get all restaurants or search by name
     */
    public List<Restaurant> getRestaurants(String search) {
        if (search == null || search.isEmpty()) {
            return restaurantRepository.findAll();
        } else {
            return restaurantRepository.findByNameContainingIgnoreCase(search);
        }
    }
    
    /**
     * Get restaurant by ID
     */
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado con ID: " + id));
    }
    
    /**
     * Create a new restaurant
     */
    public Restaurant createRestaurant(RestaurantDTO restaurantDTO) {
        // Validate that restaurant name doesn't already exist
        List<Restaurant> existing = restaurantRepository.findByNameContainingIgnoreCase(restaurantDTO.getName());
        if (!existing.isEmpty()) {
            // Check for exact match
            boolean exactMatch = existing.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(restaurantDTO.getName()));
            if (exactMatch) {
                throw new IllegalArgumentException("Ya existe un restaurante con ese nombre");
            }
        }
        
        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantDTO.getName());
        restaurant.setAddress(restaurantDTO.getAddress());
        restaurant.setDescription(restaurantDTO.getDescription());
        
        return restaurantRepository.save(restaurant);
    }
    
    /**
     * Update an existing restaurant
     */
    public Restaurant updateRestaurant(Long id, RestaurantDTO restaurantDTO) {
        Restaurant existingRestaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado con ID: " + id));
        
        // Check if name is being changed and if new name already exists
        if (!existingRestaurant.getName().equalsIgnoreCase(restaurantDTO.getName())) {
            List<Restaurant> existing = restaurantRepository.findByNameContainingIgnoreCase(restaurantDTO.getName());
            boolean exactMatch = existing.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(restaurantDTO.getName()) && !r.getId().equals(id));
            if (exactMatch) {
                throw new IllegalArgumentException("Ya existe un restaurante con ese nombre");
            }
        }
        
        existingRestaurant.setName(restaurantDTO.getName());
        existingRestaurant.setAddress(restaurantDTO.getAddress());
        existingRestaurant.setDescription(restaurantDTO.getDescription());
        
        return restaurantRepository.save(existingRestaurant);
    }
    
    /**
     * Delete a restaurant
     */
    public void deleteRestaurant(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new IllegalArgumentException("Restaurante no encontrado con ID: " + id);
        }
        restaurantRepository.deleteById(id);
    }
}
