package com.smartDine.entity;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Table ;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "dishes")
public class Dish extends MenuItem {
    
    @Enumerated(EnumType.STRING)
    private CourseType courseType;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<Element> elements;

    public Dish() {
        super();
    }

    public Dish addElement(Element element) {
        if (elements != null) this.elements.add(element);
        else {
            elements = new java.util.ArrayList<>();
            elements.add(element);
        }
        return this;
    }
    
}