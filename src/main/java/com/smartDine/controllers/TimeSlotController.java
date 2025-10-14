package com.smartDine.controllers;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.TimeSlotDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Role;
import com.smartDine.entity.TimeSlot;
import com.smartDine.entity.User;
import com.smartDine.services.TimeSlotService;


@RestController
@RequestMapping("/smartdine/api/restaurants/{restaurantId}")
public class TimeSlotController {
    @Autowired
    private TimeSlotService timeSlotService;
    
    @PostMapping("/timeslots")
    public ResponseEntity<TimeSlotDTO> postTimeSlot(@PathVariable Long restaurantId,  
                                            @RequestBody TimeSlotDTO timeSlotDTO,  
                                            @AuthenticationPrincipal User owner) {
        if (owner.getRole() != Role.ROLE_ADMIN && owner.getRole() != Role.ROLE_BUSINESS) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        timeSlotDTO.setRestaurantId(restaurantId);
        TimeSlot timeSlot = timeSlotService.createTimeSlot(timeSlotDTO, (Business) owner);
        TimeSlotDTO timeSlotResponseDTO = TimeSlotDTO.fromEntity(timeSlot);
        return ResponseEntity.ok(timeSlotResponseDTO);
    
    }
    
    @GetMapping("/timeslots")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlots(@RequestParam Long restaurantId, 
                                @AuthenticationPrincipal User user,
                                @RequestParam(name = "day") DayOfWeek dayOfWeek) { 
        List<TimeSlot> timeSlots;
        if (dayOfWeek == null) {
            timeSlots = timeSlotService.getTimeSlotsForRestaurant(restaurantId);
        } 
        else {
            timeSlots = timeSlotService.getTimeSlotsForRestaurantByDay(restaurantId, dayOfWeek);
        }
        List<TimeSlotDTO> timeSlotDTOs = TimeSlotDTO.fromEntity(timeSlots);
        return ResponseEntity.ok(timeSlotDTOs);
        
    }
    
}
