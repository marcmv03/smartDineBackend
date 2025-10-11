package com.smartDine.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Drink;
@Repository
public interface DrinkRepository extends JpaRepository<Drink, Long> {
    
}