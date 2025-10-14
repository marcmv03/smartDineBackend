package com.smartDine.dto;

import com.smartDine.entity.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class TableDTO {
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
    
    public TableDTO() {}
    
    public TableDTO(Integer number, Integer capacity, Boolean outside) {
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
    
    public static Table toEntity(TableDTO dto) {
        Table table = new Table();
        if (dto.getId() != null) {
            table.setId(dto.getId());
        }
        table.setNumber(dto.getNumber());
        table.setCapacity(dto.getCapacity());
        table.setOutside(dto.getOutside());
        return table;
    }
    
    public static TableDTO fromEntity(Table table) {
        TableDTO dto = new TableDTO();
        dto.setId(table.getId());
        dto.setNumber(table.getNumber());
        dto.setCapacity(table.getCapacity());
        dto.setOutside(table.getOutside());
        if (table.getRestaurant() != null) {
            dto.setRestaurantId(table.getRestaurant().getId());
        }
        return dto;
    }
    
    public static List<TableDTO> fromEntity(List<Table> tables) {
        return tables.stream()
            .map(TableDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
