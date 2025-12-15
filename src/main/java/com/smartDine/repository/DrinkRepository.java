package com.smartDine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Drink;
@Repository
public interface DrinkRepository extends JpaRepository<Drink, Long> {
    Optional<Drink> findByNameAndRestaurantId(String name, Long restaurantId);
}