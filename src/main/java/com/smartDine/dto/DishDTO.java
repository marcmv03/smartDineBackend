package com.smartDine.dto;

import java.util.List;

import com.smartDine.entity.CourseType;
import com.smartDine.entity.Dish;
import com.smartDine.entity.Element;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishDTO extends MenuItemDTO {
    private List<Element> elements;
    @NotNull
    private CourseType courseType;
    
    public DishDTO() {
        super();
        setItemType("DISH");
    }
    public DishDTO(Long id, String name, String description, Double price, String itemType, List<Element> elements, CourseType courseType,String imageUrl) {
        super(name, description, price, imageUrl) ;
        setItemType("DISH");
        this.elements = elements;
        this.courseType = courseType;
    }
    
    public static Dish toEntity(DishDTO dto) {
        Dish dish = new Dish();
        if (dto.getId() != null) {
            // Note: We can't set id directly on Dish, it's managed by JPA
            // The id will be set when retrieved from database
        }
        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setPrice(dto.getPrice());
        dish.setCourseType(dto.getCourseType());
        dish.setElements(dto.getElements());
        dish.setImageUrl(dto.getImageUrl());
        return dish;
    }
    
    public static DishDTO fromEntity(Dish dish) {
        DishDTO dto = new DishDTO();
        dto.setId(dish.getId());
        dto.setName(dish.getName());
        dto.setDescription(dish.getDescription());
        dto.setPrice(dish.getPrice());
        dto.setImageUrl(dish.getImageUrl()) ; 
        dto.setItemType("DISH");
        dto.setCourseType(dish.getCourseType());
        dto.setElements(dish.getElements());
        return dto;
    }
}