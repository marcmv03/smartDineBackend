package com.smartDine.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@jakarta.persistence.Table(name = "drinks")
public class Drink extends MenuItem {
    @Enumerated(EnumType.STRING)
    private DrinkType drinkType;

    public Drink() {
        super();
    }

    public Drink setDrinkType(DrinkType drinkType) {
        this.drinkType = drinkType;
        return this;
    }
}