package com.smartDine.services;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.smartDine.dto.TimeSlotDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.TimeSlot;
import com.smartDine.exceptions.RelatedEntityException;
import com.smartDine.repository.TimeSlotRepository;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private TimeSlotService timeSlotService;

    private Business owner;
    private Restaurant restaurant;
    private TimeSlot timeSlot;

    @BeforeEach
    void setUp() {
        owner = new Business("Owner", "owner@test.com", "password", 111111111L);
        owner.setId(1L);
        owner.setRestaurants(new ArrayList<>());

        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("123 Main Street");
        restaurant.setDescription("Test restaurant");
        restaurant.setOwner(owner);
        restaurant.setTimeSlots(new ArrayList<>());

        owner.getRestaurants().add(restaurant);

        timeSlot = new TimeSlot();
        timeSlot.setId(1L);
        timeSlot.setDayOfWeek(DayOfWeek.MONDAY);
        timeSlot.setStartTime(12.0);
        timeSlot.setEndTime(14.0);
        timeSlot.setRestaurant(restaurant);
    }

    @Test
    @DisplayName("Should create a time slot for the owner of the restaurant")
    void createTimeSlotForOwner() {
        TimeSlotDTO request = buildTimeSlotDTO(restaurant.getId(), DayOfWeek.MONDAY, 12.0, 14.0);

        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(timeSlotRepository.existsByRestaurantIdAndDayOfWeekAndStartTimeAndEndTime(
            restaurant.getId(), DayOfWeek.MONDAY, 12.0, 14.0)).thenReturn(false);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);
        when(restaurantService.addTimeSlot(any(TimeSlot.class))).thenReturn(timeSlot);

        TimeSlot created = timeSlotService.createTimeSlot(request, owner);

        assertNotNull(created);
        assertEquals(timeSlot.getId(), created.getId());
        assertEquals(restaurant.getId(), created.getRestaurant().getId());
        verify(timeSlotRepository).save(any(TimeSlot.class));
    }

    @Test
    @DisplayName("Should reject time slot creation when business is not the owner")
    void createTimeSlotWithNonOwner() {
        Business anotherBusiness = new Business("Intruder", "intruder@test.com", "password", 333333333L);
        anotherBusiness.setId(2L);

        TimeSlotDTO request = buildTimeSlotDTO(restaurant.getId(), DayOfWeek.TUESDAY, 18.0, 20.0);

        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), anotherBusiness)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> timeSlotService.createTimeSlot(request, anotherBusiness)
        );

        assertEquals("Business is not the owner of the restaurant", exception.getMessage());
        verify(timeSlotRepository, never()).save(any(TimeSlot.class));
    }

    @Test
    @DisplayName("Should reject duplicated time slots for the same restaurant, day, start, and end time")
    void createDuplicatedTimeSlot() {
        TimeSlotDTO request = buildTimeSlotDTO(restaurant.getId(), DayOfWeek.FRIDAY, 10.0, 12.0);

        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(timeSlotRepository.existsByRestaurantIdAndDayOfWeekAndStartTimeAndEndTime(
            restaurant.getId(), DayOfWeek.FRIDAY, 10.0, 12.0)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> timeSlotService.createTimeSlot(request, owner)
        );

        assertEquals("A time slot with the same start and end time already exists for this day", exception.getMessage());
        verify(timeSlotRepository, never()).save(any(TimeSlot.class));
    }

    @Test
    @DisplayName("Should delete time slot successfully")
    void deleteTimeSlotSuccess() {
        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(timeSlotRepository.findById(timeSlot.getId())).thenReturn(Optional.of(timeSlot));
        doNothing().when(timeSlotRepository).delete(timeSlot);
        doNothing().when(timeSlotRepository).flush();

        timeSlotService.deleteTimeSlot(restaurant.getId(), timeSlot.getId(), owner);

        verify(timeSlotRepository).delete(timeSlot);
        verify(timeSlotRepository).flush();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent time slot")
    void deleteNonExistentTimeSlot() {
        Long nonExistentId = 999L;

        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(timeSlotRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> timeSlotService.deleteTimeSlot(restaurant.getId(), nonExistentId, owner)
        );

        assertEquals("Time slot not found with id: " + nonExistentId, exception.getMessage());
        verify(timeSlotRepository, never()).delete(any(TimeSlot.class));
    }

    @Test
    @DisplayName("Should throw RelatedEntityException when deleting time slot with reservations")
    void deleteTimeSlotWithReservations() {
        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(timeSlotRepository.findById(timeSlot.getId())).thenReturn(Optional.of(timeSlot));
        doNothing().when(timeSlotRepository).delete(timeSlot);
        doThrow(new DataIntegrityViolationException("Foreign key constraint violation"))
            .when(timeSlotRepository).flush();

        RelatedEntityException exception = assertThrows(
            RelatedEntityException.class,
            () -> timeSlotService.deleteTimeSlot(restaurant.getId(), timeSlot.getId(), owner)
        );

        assertEquals("No se puede eliminar el intervalo de tiempo porque tiene reservas asociadas.", exception.getMessage());
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
