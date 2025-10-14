package com.smartDine.dto;

import java.util.List;

import com.smartDine.entity.CourseType;
import com.smartDine.entity.Dish;
import com.smartDine.entity.Element;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishDTO extends MenuItemDTO {
    private List<Element> elements;
    private CourseType courseType;
    
    public DishDTO() {
        super();
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
        return dish;
    }
    
    public static DishDTO fromEntity(Dish dish) {
        DishDTO dto = new DishDTO();
        dto.setId(dish.getId());
        dto.setName(dish.getName());
        dto.setDescription(dish.getDescription());
        dto.setPrice(dish.getPrice());
        dto.setItemType("DISH");
        dto.setCourseType(dish.getCourseType());
        dto.setElements(dish.getElements());
        return dto;
    }
}