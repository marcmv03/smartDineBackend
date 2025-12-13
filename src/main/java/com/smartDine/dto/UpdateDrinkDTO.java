package com.smartDine.dto;

import com.smartDine.entity.DrinkType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDrinkDTO {
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Drink type is required")
    private DrinkType drinkType;
}
