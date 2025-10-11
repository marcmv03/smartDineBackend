package com.smartDine.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.TimeSlotDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.TimeSlot;
import com.smartDine.repository.TimeSlotRepository;

@Service
public class TimeSlotService {
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    @Autowired
    private RestaurantService restaurantService;

    @Transactional
    public TimeSlot createTimeSlot(TimeSlotDTO timeSlotDTO, Business business) {
        if (business == null || business.getId() == null) {
            throw new IllegalArgumentException("Business owner is required to create a time slot");
        }

        Restaurant restaurant = restaurantService.getRestaurantById(timeSlotDTO.getRestaurantId());
        if (!restaurantService.isOwnerOfRestaurant(restaurant.getId(), business)) {
            throw new IllegalArgumentException("Business is not the owner of the restaurant");
        }

        boolean timeSlotExists = timeSlotRepository.existsByRestaurantIdAndDayOfWeekAndStartTimeAndEndTime(
            restaurant.getId(),
            timeSlotDTO.getDayOfWeek(),
            timeSlotDTO.getStartTime(),
            timeSlotDTO.getEndTime()
        );

        if (timeSlotExists) {
            throw new IllegalArgumentException("A time slot with the same start and end time already exists for this day");
        }

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(timeSlotDTO.getStartTime());
        timeSlot.setEndTime(timeSlotDTO.getEndTime());
        timeSlot.setDayOfWeek(timeSlotDTO.getDayOfWeek());
        timeSlot.setRestaurant(restaurant);

        TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);
        restaurantService.addTimeSlot(savedTimeSlot);
        return savedTimeSlot;
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> getTimeSlotsForRestaurant(Long restaurantId) {
        restaurantService.getRestaurantById(restaurantId);
        return restaurantService.getTimeSlots(restaurantId);
    }
    @Transactional(readOnly = true)
    public List<TimeSlot> getTimeSlotsForRestaurantByDay(Long restaurantId, java.time.DayOfWeek dayOfWeek) {
        return timeSlotRepository.findByRestaurantIdAndDayOfWeek(restaurantId, dayOfWeek);
}
}
