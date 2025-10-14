package com.smartDine.repository;

import com.smartDine.entity.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<Table, Long> {
    List<Table> findByRestaurantId(Long restaurantId);
    boolean existsByRestaurantIdAndNumber(Long restaurantId, Integer number);
}
