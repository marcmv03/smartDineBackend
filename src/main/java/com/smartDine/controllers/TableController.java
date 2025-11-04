package com.smartDine.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.RestaurantTableDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.User;
import com.smartDine.services.RestaurantTableService;

import jakarta.validation.Valid;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/smartdine/api/restaurants/{restaurantId}/tables")
public class TableController {
    
    @Autowired
    private RestaurantTableService tableService;
    
    /**
     * POST /smartdine/api/restaurants/{restaurantId}/tables - Create a new table for a restaurant
     */
    @PostMapping
    public ResponseEntity<RestaurantTableDTO> postTable(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantTableDTO tableDTO,
            @AuthenticationPrincipal User user) {
        
        // Validate that user is not null
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Validate that user is a Business
        if (!(user instanceof Business)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Validate that tableDTO is not null (already validated by @Valid)
        if (tableDTO == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Set the restaurantId in the DTO
        tableDTO.setRestaurantId(restaurantId);
        
        // Create the table
        RestaurantTable createdTable = tableService.createTable(restaurantId, tableDTO, (Business) user);
        
        // Convert entity to DTO and return
        RestaurantTableDTO responseDTO = RestaurantTableDTO.fromEntity(createdTable);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
    
    /**
     * GET /smartdine/api/restaurants/{restaurantId}/tables - Get all tables for a restaurant
     */
    @GetMapping
    public ResponseEntity<List<RestaurantTableDTO>> getAvaliableTables(@PathVariable Long  restaurantId, @RequestParam LocalDate date,@RequestParam Long  timeSlot, @RequestParam Boolean outside)  {
         List<RestaurantTableDTO> avaliableTables = tableService.getAvailableTables(restaurantId, timeSlot, date, outside) ;
         return ResponseEntity.ok(avaliableTables);

    }
    
    @GetMapping
    public ResponseEntity<List<RestaurantTableDTO>> getRestaurantTables(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal User user) {
        
        // Validate that user is not null
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Validate that user is a Business
        if (!(user instanceof Business)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Get the tables
        List<RestaurantTable> tables = tableService.getTablesByRestaurant(restaurantId, (Business) user);
        
        // Convert entities to DTOs and return
        List<RestaurantTableDTO> tableDTOs = RestaurantTableDTO.fromEntity(tables);
        return ResponseEntity.ok(tableDTOs);
    }
}
