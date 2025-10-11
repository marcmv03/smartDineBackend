package com.smartDine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.smartDine.dto.TimeSlotDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.TimeSlot;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.RestaurantRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class TimeSlotServiceTest {

    @Autowired
    private TimeSlotService timeSlotService;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    @DisplayName("Should create a time slot for the owner of the restaurant")
    void createTimeSlotForOwner() {
        Business owner = createBusiness("Owner One", "owner1@smartdine.com", 111111111L);
        Restaurant restaurant = createRestaurant(owner, "Owner Restaurant");

        TimeSlotDTO request = buildTimeSlotDTO(restaurant.getId(), DayOfWeek.MONDAY, 12.0, 14.0);
        TimeSlot created = timeSlotService.createTimeSlot(request, owner);

        assertNotNull(created.getId());
        assertEquals(restaurant.getId(), created.getRestaurant().getId());

        List<TimeSlot> timeSlots = timeSlotService.getTimeSlotsForRestaurant(restaurant.getId());
        assertEquals(1, timeSlots.size());
        assertEquals(created.getId(), timeSlots.get(0).getId());
    }

    @Test
    @DisplayName("Should reject time slot creation when business is not the owner")
    void createTimeSlotWithNonOwner() {
        Business owner = createBusiness("Owner Two", "owner2@smartdine.com", 222222222L);
        Restaurant restaurant = createRestaurant(owner, "Owner Two Restaurant");
        Business anotherBusiness = createBusiness("Intruder", "intruder@smartdine.com", 333333333L);

        TimeSlotDTO request = buildTimeSlotDTO(restaurant.getId(), DayOfWeek.TUESDAY, 18.0, 20.0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> timeSlotService.createTimeSlot(request, anotherBusiness)
        );

        assertEquals("Business is not the owner of the restaurant", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject duplicated time slots for the same restaurant, day, start, and end time")
    void createDuplicatedTimeSlot() {
        Business owner = createBusiness("Owner Three", "owner3@smartdine.com", 444444444L);
        Restaurant restaurant = createRestaurant(owner, "Owner Three Restaurant");

        TimeSlotDTO request = buildTimeSlotDTO(restaurant.getId(), DayOfWeek.FRIDAY, 10.0, 12.0);
        TimeSlot first = timeSlotService.createTimeSlot(request, owner);
        assertNotNull(first.getId());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> timeSlotService.createTimeSlot(request, owner)
        );

        assertEquals("A time slot with the same start and end time already exists for this day", exception.getMessage());
    }

    private Business createBusiness(String name, String email, Long phoneNumber) {
        Business business = new Business(name, email, "password", phoneNumber);
        business.setRestaurants(new ArrayList<>());
        return businessRepository.save(business);
    }

    private Restaurant createRestaurant(Business owner, String restaurantName) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantName);
        restaurant.setAddress("123 Main Street");
        restaurant.setDescription("Test restaurant");
        restaurant.setOwner(owner);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        owner.getRestaurants().add(savedRestaurant);
        businessRepository.save(owner);
        return savedRestaurant;
    }

    private TimeSlotDTO buildTimeSlotDTO(Long restaurantId, DayOfWeek dayOfWeek, double start, double end) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setRestaurantId(restaurantId);
        dto.setDayOfWeek(dayOfWeek);
        dto.setStartTime(start);
        dto.setEndTime(end);
        return dto;
    }
}
