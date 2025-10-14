package com.smartDine.dto;

import com.smartDine.entity.Drink;
import com.smartDine.entity.DrinkType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrinkDTO extends MenuItemDTO {
    private DrinkType drinkType;
    
    public DrinkDTO() {
        super();
    }
    
    public static Drink toEntity(DrinkDTO dto) {
        Drink drink = new Drink();
        if (dto.getId() != null) {
            // Note: We can't set id directly on Drink, it's managed by JPA
            // The id will be set when retrieved from database
        }
        drink.setName(dto.getName());
        drink.setDescription(dto.getDescription());
        drink.setPrice(dto.getPrice());
        drink.setDrinkType(dto.getDrinkType());
        return drink;
    }
    
    public static DrinkDTO fromEntity(Drink drink) {
        DrinkDTO dto = new DrinkDTO();
        dto.setId(drink.getId());
        dto.setName(drink.getName());
        dto.setDescription(drink.getDescription());
        dto.setPrice(drink.getPrice());
        dto.setItemType("DRINK");
        dto.setDrinkType(drink.getDrinkType());
        return dto;
    }
}