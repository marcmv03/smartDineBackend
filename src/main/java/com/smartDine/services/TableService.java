package com.smartDine.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.TableDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.Table;
import com.smartDine.repository.TableRepository;

@Service
public class TableService {
    @Autowired
    private TableRepository tableRepository;
    
    @Autowired
    private RestaurantService restaurantService;
    
    @Transactional
    public Table createTable(Long restaurantId, TableDTO tableDTO, Business business) {
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
        Table table = TableDTO.toEntity(tableDTO);
        table.setRestaurant(restaurant);
        
        // Save the table
        Table savedTable = tableRepository.save(table);
        
        // Add the table to the restaurant's list
        restaurantService.addTable(restaurantId, savedTable);
        
        return savedTable;
    }
    
    @Transactional(readOnly = true)
    public List<Table> getTablesByRestaurant(Long restaurantId, Business business) {
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
}
