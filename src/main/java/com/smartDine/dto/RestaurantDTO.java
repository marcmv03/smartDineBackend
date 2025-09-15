package com.smartDine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RestaurantDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    @NotBlank(message = "La dirección es obligatoria")
    private String address;
    
    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String description;
    
    // Default constructor
    public RestaurantDTO() {}
    
    public RestaurantDTO(String name, String address, String description) {
        this.name = name;
        this.address = address;
        this.description = description;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    public void setDescription(String description) {
        this.description = description;
    }
    
}
