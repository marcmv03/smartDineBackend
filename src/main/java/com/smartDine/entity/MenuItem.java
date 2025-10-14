package com.smartDine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@jakarta.persistence.Table(name = "menu_items")
public abstract class MenuItem {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Double price;

    public MenuItem setName(String name) {
        this.name = name;
        return this;
    }
    
    public MenuItem setDescription(String description) {
        this.description = description;
        return this;
    }
    
    public MenuItem setPrice(Double price) {
        this.price = price;
        return this;
    }
}
