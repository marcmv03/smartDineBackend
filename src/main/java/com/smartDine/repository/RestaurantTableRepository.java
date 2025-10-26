package com.smartDine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.RestaurantTable;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByRestaurantId(Long restaurantId);
    boolean existsByRestaurantIdAndNumber(Long restaurantId, Integer number);
    
    @Query("SELECT t FROM RestaurantTable t JOIN FETCH t.restaurant WHERE t.restaurant.id = :restaurantId AND t.capacity >= :capacity")
    List<RestaurantTable> findByRestaurantIdAndCapacityGreaterThanEqual(@Param("restaurantId") Long restaurantId, @Param("capacity") Integer capacity);
}
