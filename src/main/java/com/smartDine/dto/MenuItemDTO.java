package com.smartDine.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DishDTO.class, name = "DISH"),
        @JsonSubTypes.Type(value = DrinkDTO.class, name = "DRINK")
})

public abstract class MenuItemDTO {
    @NotBlank
    private String name ;
    @NotBlank
    private String description ;
    @Min(value = 0, message = "price must be positive")
    private Double price ;
    private String imageUrl ;
    @NotNull
    private String itemType ;
    public MenuItemDTO(String name, String description, Double price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }
    public MenuItemDTO() {}


    public String getName() {
        return name;
    }

    public MenuItemDTO setName(String name) {
        this.name = name;
        return this ;
    }

    public String getDescription() {
        return description;
    }

    public MenuItemDTO setDescription(String description) {
        this.description = description;
        return this ;
    }

    public Double getPrice() {
        return price;
    }

    public MenuItemDTO setPrice(Double price) {
        this.price = price;
        return this ;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public MenuItemDTO setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this ;
    }

    public String getItemType() {
        return itemType;
    }

    public MenuItemDTO setItemType(String itemType) {
        this.itemType = itemType;
        return this ;
    }
}
