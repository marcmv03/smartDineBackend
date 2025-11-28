package com.smartDine.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.RestaurantTableDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.exceptions.RelatedEntityException;
import com.smartDine.repository.ReservationRepository;
import com.smartDine.repository.RestaurantTableRepository;

@Service
public class RestaurantTableService {
    @Autowired
    private RestaurantTableRepository tableRepository;
    
    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private ReservationRepository reservationRepository; // ← Cambiado de ReservationService a ReservationRepository
    
    @Transactional
    public RestaurantTable createTable(Long restaurantId, RestaurantTableDTO tableDTO, Business business) {
        if (business == null || business.getId() == null) {
            throw new IllegalArgumentException("Business owner is required to create a table");
        }
        
        // Verify that the restaurant exists
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        
        // Verify that the business is the owner of the restaurant
        if (!restaurantService.isOwnerOfRestaurant(restaurant.getId(), business)) {
            throw new IllegalArgumentException("Business is not the owner of the restaurant");
        }
        
        // Verify that the table number is not already used in this restaurant
        boolean tableExists = tableRepository.existsByRestaurantIdAndNumber(restaurantId, tableDTO.getNumber());
        if (tableExists) {
            throw new IllegalArgumentException("A table with number " + tableDTO.getNumber() + " already exists in this restaurant");
        }
        
        // Create the table entity from DTO
        RestaurantTable table = RestaurantTableDTO.toEntity(tableDTO);
        table.setRestaurant(restaurant);
        
        // Save the table
        RestaurantTable savedTable = tableRepository.save(table);
        
        // Add the table to the restaurant's list
        restaurantService.addTable(restaurantId, savedTable);
        
        return savedTable;
    }
    
    @Transactional(readOnly = true)
    public List<RestaurantTable> getTablesByRestaurant(Long restaurantId, Business business) {
        if (business == null || business.getId() == null) {
            throw new IllegalArgumentException("Business owner is required to get tables");
        }
        
        // Verify that the restaurant exists
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        
        // Verify that the business is the owner of the restaurant
        if (!restaurantService.isOwnerOfRestaurant(restaurant.getId(), business)) {
            throw new IllegalArgumentException("Business is not the owner of the restaurant");
        }
        
        return restaurantService.getTables(restaurantId);
    }
    
    @Transactional(readOnly = true) 
    public List<RestaurantTableDTO> getAvailableTables(Long restaurantId, Long timeSlotId, LocalDate date, Boolean outside) {
        // Get all tables for the restaurant
        List<RestaurantTable> restaurantTables = restaurantService.getTables(restaurantId);
        
        // Get all reservations for the given date and time slot using repository directly
        List<Reservation> reservations = reservationRepository.findByRestaurantIdAndDateAndTimeSlotId(
            restaurantId, 
            date, 
            timeSlotId
        );
        
        // Filter available tables
        List<RestaurantTable> availableTables = restaurantTables.stream()
            .filter(table -> {
                // Check if table matches outside/inside preference
                if (!table.getOutside().equals(outside)) {
                    return false;
                }
                
                // Check if table is not reserved
                boolean isReserved = reservations.stream()
                    .anyMatch(reservation -> 
                        reservation.getRestaurantTable().getId().equals(table.getId())
                    );
                
                return !isReserved;
            })
            .collect(Collectors.toList());
        
        return RestaurantTableDTO.fromEntity(availableTables);
    }
    
    @Transactional(readOnly = true)
    public RestaurantTable getTableById(Long tableId) {
        return tableRepository.findById(tableId)
            .orElseThrow(() -> new IllegalArgumentException("Table not found with id: " + tableId));
    }
    
    @Transactional
    public void deleteTable(Long restaurantId, Long tableId, Business business)throws RelatedEntityException {
        try {
        if (business == null || business.getId() == null) {
            throw new IllegalArgumentException("Business owner is required to delete a table");
        }
        
        // Verify that the restaurant exists
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        
        // Verify that the business is the owner of the restaurant
        if (!restaurantService.isOwnerOfRestaurant(restaurant.getId(), business)) {
            throw new IllegalArgumentException("Business is not the owner of the restaurant");
        }
        
        // Verify that the table exists
        RestaurantTable table = getTableById(tableId);
        
        // Verify that the table belongs to the restaurant
        if (!table.getRestaurant().getId().equals(restaurantId)) {
            throw new IllegalArgumentException("Table does not belong to the specified restaurant");
        }
        
        // Delete the table
        tableRepository.delete(table);
        tableRepository.flush(); // Forzar la ejecución inmediata para capturar excepciones de integridad referencial
    }
    catch(DataIntegrityViolationException e ) {
                throw new RelatedEntityException("No se puede eliminar la mesa porque tiene reservas asociadas.");


    }
}
}