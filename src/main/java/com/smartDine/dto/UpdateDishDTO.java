package com.smartDine.dto;

import java.util.List;

import com.smartDine.entity.CourseType;
import com.smartDine.entity.Element;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDishDTO {
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Course type is required")
    private CourseType courseType;
    
    private List<Element> elements;
}
