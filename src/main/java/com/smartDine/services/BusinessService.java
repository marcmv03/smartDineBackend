package com.smartDine.services;

import com.smartDine.entity.Business;
import com.smartDine.entity.Restaurant;
import com.smartDine.dto.RestaurantDTO;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BusinessService {

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Registers a new business.
     *
     * @param name     The business name.
     * @param email    The business email address.
     * @param password The business password.
     * @param number   The business phone number.
     * @return The created business object.
     * @throws IllegalArgumentException if a business with the same email or phone number already exists.
     */
    public Business createBusiness(String name, String email, String password, Long number) {
        Optional<Business> existingBusiness = businessRepository.findByEmailOrPhoneNumber(email, number);
        if (existingBusiness.isPresent()) {
            throw new IllegalArgumentException("Ya existe un negocio con este correo electrónico o número de teléfono.");
        }
        
        // Encrypt password before saving
        String encryptedPassword = passwordEncoder.encode(password);
        Business newBusiness = new Business();
        newBusiness.setName(name);
        newBusiness.setEmail(email);
        newBusiness.setPassword(encryptedPassword);
        newBusiness.setPhoneNumber(number);
        return businessRepository.save(newBusiness);
    }

   public Business getBusinessById(Long id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Business not found with id: " + id));
    }

    /**
     * Updates a business's information.
     *
     * @param businessId The ID of the business to update.
     * @param name       The new name (optional).
     * @param email      The new email (optional).
     * @param number     The new phone number (optional).
     * @return The updated business object.
     * @throws IllegalArgumentException if the business is not found or if there are validation errors.
     */
    public Business updateBusiness(Long businessId, String name, String email, Long number) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado."));

        // Check for duplicates if email or number are being changed
        if (email != null && !email.equals(business.getEmail())) {
            Optional<Business> existingByEmail = businessRepository.findByEmail(email);
            if (existingByEmail.isPresent()) {
                throw new IllegalArgumentException("Ya existe un negocio con este correo electrónico.");
            }
            business.setEmail(email);
        }

        if (number != null && !number.equals(business.getPhoneNumber())) {
            Optional<Business> existingByNumber = businessRepository.findByPhoneNumber(number);
            if (existingByNumber.isPresent()) {
                throw new IllegalArgumentException("Ya existe un negocio con este número de teléfono.");
            }
            business.setPhoneNumber(number);
        }

        if (name != null) {
            business.setName(name);
        }

        return businessRepository.save(business);
    }

    /**
     * Updates a business's password. 
     *
     * @param businessId  The ID of the business.
     * @param oldPassword The current password.
     * @param newPassword The new password.
     * @throws IllegalArgumentException if the business is not found or old password is incorrect.
     */
    public void updatePassword(Long businessId, String oldPassword, String newPassword) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado."));

        if (!passwordEncoder.matches(oldPassword, business.getPassword())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta.");
        }

        business.setPassword(passwordEncoder.encode(newPassword));
        businessRepository.save(business);
    }

    /**
     * Deletes a business by ID.
     *
     * @param businessId The ID of the business to delete.
     * @throws IllegalArgumentException if the business is not found.
     */
    public void deleteBusiness(Long businessId) {
        if (!businessRepository.existsById(businessId)) {
            throw new IllegalArgumentException("Negocio no encontrado.");
        }
        businessRepository.deleteById(businessId);
    }

    /**
     * Finds businesses by name or returns all if no search parameter is provided.
     *
     * @param search The name or part of the name to search for.
     * @return A list of businesses.
     */
    public List<Business> findBusinesses(String search) {
        if (search == null || search.trim().isEmpty()) {
            return businessRepository.findAll();
        }
        return businessRepository.findByNameContainingIgnoreCase(search.trim());
    }

    /**
     * Finds a business by email.
     *
     * @param email The business email.
     * @return Optional containing the business if found.
     */
    public Optional<Business> findByEmail(String email) {
        return businessRepository.findByEmail(email);
    }

    /**
     * Finds a business by ID.
     *
     * @param businessId The business ID.
     * @return Optional containing the business if found.
     */
    public Optional<Business> findById(Long businessId) {
        return businessRepository.findById(businessId);
    }

    /**
     * Crea un restaurante y lo asocia al propietario (Business)
     */
    public Restaurant createRestaurantForBusiness(Business owner, RestaurantDTO restaurantDTO) {
        // Validar que el nombre del restaurante no exista ya
        List<Restaurant> existing = restaurantRepository.findByNameContainingIgnoreCase(restaurantDTO.getName());
        boolean exactMatch = existing.stream()
            .anyMatch(r -> r.getName().equalsIgnoreCase(restaurantDTO.getName()));
        if (exactMatch) {
            throw new IllegalArgumentException("Ya existe un restaurante con ese nombre");
        }
        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantDTO.getName());
        restaurant.setAddress(restaurantDTO.getAddress());
        restaurant.setDescription(restaurantDTO.getDescription());
        restaurant.setOwner(owner);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        List<Restaurant> restaurants = owner.getRestaurants();
        if (restaurants == null) {
            restaurants = new java.util.ArrayList<>();
        }
        restaurants.add(savedRestaurant);
        owner.setRestaurants(restaurants);
        businessRepository.save(owner);
        return savedRestaurant;
    }
}
