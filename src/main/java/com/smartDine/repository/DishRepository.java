package com.smartDine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Dish;
@Repository
public interface  DishRepository extends  JpaRepository<Dish, Long> {
    Optional<Dish> findByNameAndRestaurantId(String name, Long restaurantId);
}
