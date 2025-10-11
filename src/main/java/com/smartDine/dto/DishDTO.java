package com.smartDine.dto ;

import java.util.List;

import com.smartDine.entity.CourseType;
import com.smartDine.entity.Element;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class DishDTO extends MenuItemDTO {
    private List<Element> elements ;
    private CourseType courseType ;
    public DishDTO() {
        super();
    }
}