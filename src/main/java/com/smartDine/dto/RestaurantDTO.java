package com.smartDine.dto;
import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.Restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RestaurantDTO {
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    @NotBlank(message = "La dirección es obligatoria")
    private String address;
    
    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String description;
    
    String imageUrl;
    // Default constructor
    public RestaurantDTO() {}
    
    public RestaurantDTO(String name, String address, String description) {
        this.name = name;
        this.address = address;
        this.description = description;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public RestaurantDTO setName(String name) {
        this.name = name;
        return this;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getDescription() {
        return description;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    
    public RestaurantDTO setDescription(String description) {
        this.description = description;
        return this;
    }
    
    public static Restaurant toEntity(RestaurantDTO dto) {
        Restaurant restaurant = new Restaurant();
        if (dto.getId() != null) {
            restaurant.setId(dto.getId());
        }
        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setDescription(dto.getDescription());
        restaurant.setImageUrl(dto.getImageUrl());
        return restaurant;
    }
    
    public static RestaurantDTO fromEntity(Restaurant restaurant) {
        RestaurantDTO dto = new RestaurantDTO();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setDescription(restaurant.getDescription());
        dto.setImageUrl(restaurant.getImageUrl());
        return dto;
    }
    
    private void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static List<RestaurantDTO> fromEntity(List<Restaurant> restaurants) {
        return restaurants.stream()
            .map(RestaurantDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
