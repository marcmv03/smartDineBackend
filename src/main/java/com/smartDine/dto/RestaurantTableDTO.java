package com.smartDine.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.RestaurantTable;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RestaurantTableDTO {
    private Long id;
    
    @NotNull(message = "Table number is required")
    @Min(value = 1, message = "Table number must be at least 1")
    private Integer number;
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    @NotNull(message = "Outside field is required")
    private Boolean outside;
    
    private Long restaurantId;
    
    public RestaurantTableDTO() {}
    
    public RestaurantTableDTO(Integer number, Integer capacity, Boolean outside) {
        this.number = number;
        this.capacity = capacity;
        this.outside = outside;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getNumber() {
        return number;
    }
    
    public void setNumber(Integer number) {
        this.number = number;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public Boolean getOutside() {
        return outside;
    }
    
    public void setOutside(Boolean outside) {
        this.outside = outside;
    }
    
    public Long getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public static RestaurantTable toEntity(RestaurantTableDTO dto) {
        RestaurantTable table = new RestaurantTable();
        if (dto.getId() != null) {
            table.setId(dto.getId());
        }
        table.setNumber(dto.getNumber());
        table.setCapacity(dto.getCapacity());
        table.setOutside(dto.getOutside());
        return table;
    }
    
    public static RestaurantTableDTO fromEntity(RestaurantTable table) {
        RestaurantTableDTO dto = new RestaurantTableDTO();
        dto.setId(table.getId());
        dto.setNumber(table.getNumber());
        dto.setCapacity(table.getCapacity());
        dto.setOutside(table.getOutside());
        if (table.getRestaurant() != null) {
            dto.setRestaurantId(table.getRestaurant().getId());
        }
        return dto;
    }
    
    public static List<RestaurantTableDTO> fromEntity(List<RestaurantTable> tables) {
        return tables.stream()
            .map(RestaurantTableDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
