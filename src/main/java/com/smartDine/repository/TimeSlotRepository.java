package com.smartDine.repository;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.TimeSlot;
@Repository
public interface  TimeSlotRepository extends  JpaRepository<TimeSlot, Long> {
    boolean existsByRestaurantIdAndDayOfWeekAndStartTimeAndEndTime(Long restaurantId, DayOfWeek dayOfWeek, Double startTime, Double endTime);
    List<TimeSlot> findByRestaurantId(Long restaurantId);
    List<TimeSlot> findByRestaurantIdAndDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek);
}
