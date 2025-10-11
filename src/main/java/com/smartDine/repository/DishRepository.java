package com.smartDine.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Dish;
@Repository
public interface  DishRepository extends  JpaRepository<Dish, Long> {
    
}
