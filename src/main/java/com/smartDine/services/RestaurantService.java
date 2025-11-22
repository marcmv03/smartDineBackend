package com.smartDine.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartDine.adapters.ImageAdapter;
import com.smartDine.dto.RestaurantDTO;
import com.smartDine.dto.UploadResponse;
import com.smartDine.entity.Business;
import com.smartDine.entity.MenuItem;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.TimeSlot;
import com.smartDine.repository.RestaurantRepository;

@Service 
public class RestaurantService {
    @Autowired 
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private ImageAdapter imageAdapter;
    
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
    public Restaurant createRestaurant(RestaurantDTO restaurantDTO , Business owner) {
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
        
        Restaurant restaurant = RestaurantDTO.toEntity(restaurantDTO);
        restaurant.setOwner(owner);
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

    public boolean isOwnerOfRestaurant(Long restaurantId, Business business) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with  ID: " + restaurantId));
        return restaurant.getOwner().getId().equals(business.getId());
    }

    public Restaurant createRestaurant(Business owner, RestaurantDTO restaurantDTO) {
        Restaurant restaurant = RestaurantDTO.toEntity(restaurantDTO);
        restaurant.setOwner(owner);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        // Añadir el restaurante a la lista del propietario
        List<Restaurant> restaurants = owner.getRestaurants();
        if (restaurants == null) {
            restaurants = new java.util.ArrayList<>();
        }
        restaurants.add(savedRestaurant);
        owner.setRestaurants(restaurants);
        // Si tienes un repositorio de Business, guarda el owner actualizado
        // businessRepository.save(owner);
        return savedRestaurant;
    }
    public boolean  addMenuItem(Long restaurantId, MenuItem menuItem) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with ID: " + restaurantId));
        List<MenuItem> menu = restaurant.getMenu();
        if (menu == null) {
            menu = new java.util.ArrayList<>();
        }
        menu.add(menuItem);
        restaurant.setMenu(menu);
        restaurantRepository.save(restaurant);
        return true;
    }
    public List<MenuItem> getMenuItems(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with ID: " + restaurantId));
        return restaurant.getMenu();
    }

    public TimeSlot addTimeSlot(TimeSlot timeSlot) {
        if (timeSlot == null || timeSlot.getRestaurant() == null || timeSlot.getRestaurant().getId() == null) {
            throw new IllegalArgumentException("TimeSlot must be associated with a restaurant");
        }
        Restaurant restaurant = restaurantRepository.findById(timeSlot.getRestaurant().getId())
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with ID: " + timeSlot.getRestaurant().getId()));
        restaurant.getTimeSlots().add(timeSlot);
        timeSlot.setRestaurant(restaurant);
        restaurantRepository.save(restaurant);
        return timeSlot;
    }

    public List<TimeSlot> getTimeSlots(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with ID: " + restaurantId));
        return restaurant.getTimeSlots();
    }
    
    public boolean addTable(Long restaurantId, com.smartDine.entity.RestaurantTable table) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with ID: " + restaurantId));
        List<com.smartDine.entity.RestaurantTable> tables = restaurant.getTables();
        if (tables == null) {
            tables = new java.util.ArrayList<>();
        }
        tables.add(table);
        restaurant.setTables(tables);
        restaurantRepository.save(restaurant);
        return true;
    }
    
    public List<RestaurantTable> getTables(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with ID: " + restaurantId));
        return restaurant.getTables();
    }
    
    /**
     * Uploads an image for a restaurant and assigns it to the restaurant entity.
     * Validates that the business user owns the restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @param file the image file to upload
     * @param business the business owner
     * @return UploadResponse containing upload details
     * @throws IOException if the upload fails
     * @throws IllegalArgumentException if validation fails
     */
    public UploadResponse uploadRestaurantImage(Long restaurantId, MultipartFile file, Business business) throws IOException {
        // Validate restaurant ownership
        if (!isOwnerOfRestaurant(restaurantId, business)) {
            throw new IllegalArgumentException("You do not own this restaurant");
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
        String keyName = "restaurants/%d/images/%s.%s"
            .formatted(restaurantId, java.util.UUID.randomUUID(), ext);
        
        // Upload image using the adapter
        UploadResponse response = imageAdapter.uploadImage(file, keyName);
        
        // Assign the image to the restaurant
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with ID: " + restaurantId));
        restaurant.setImageUrl(keyName);
        restaurantRepository.save(restaurant);
        
        return response;
    }

    public List<Restaurant> getRestaurantsByOwner(Business business) {
      return restaurantRepository.findByOwner(business);
    }
}
