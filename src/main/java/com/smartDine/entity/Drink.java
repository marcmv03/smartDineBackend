package com.smartDine.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Entity
@Getter
@Table(name = "drinks")
public class Drink extends MenuItem {
    @Enumerated(EnumType.STRING)
    private DrinkType drinkType;

    public Drink(Long id, String name, String description, Double price) {
        super(id, name, description, price);
    }

    public Drink() {

    }

    public Drink  setDrinkType(DrinkType drinkType) {
        this.drinkType = drinkType;
        return this ;
    }
}