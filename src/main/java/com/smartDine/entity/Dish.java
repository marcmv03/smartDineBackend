package com.smartDine.entity;
import java.util.List;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Getter;

@Entity
@Getter
@Table(name = "dishes")
public class Dish extends MenuItem {
    
    @Enumerated(EnumType.STRING)
    private CourseType courseType;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<Element> elements;

    public Dish(Long id, String name, String description, Double price) {
        super(id, name, description, price);
    }
    public Dish() {
        super() ;
    }

    public Dish addElement(Element element) {
        this.elements.add(element);
        return this;
    }
    public Dish setCourseType(CourseType courseType) {
        this.courseType = courseType;
        return this;
    }
    public Dish setElements(List<Element> elements) {
        this.elements = elements;
        return this;
    }
}