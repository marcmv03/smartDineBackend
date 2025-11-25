package com.smartDine.controllers;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import jakarta.validation.Valid;


@RestController
@RequestMapping("/smartdine/api/restaurants/{restaurantId}")
public class TimeSlotController {
    @Autowired
    private TimeSlotService timeSlotService;
    
    @PostMapping("/timeslots")
    public ResponseEntity<TimeSlotDTO> postTimeSlot(@PathVariable Long restaurantId,  
                                            @Valid @RequestBody TimeSlotDTO timeSlotDTO,  
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
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlots(@PathVariable Long restaurantId, 
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
    
    /**
     * DELETE /smartdine/api/restaurants/{restaurantId}/timeslots/{timeSlotId} - Delete a time slot
     */
    @DeleteMapping("/timeslots/{timeSlotId}")
    public ResponseEntity<Void> deleteTimeSlot(
            @PathVariable Long restaurantId,
            @PathVariable Long timeSlotId,
            @AuthenticationPrincipal User user) {
        
        // Validate user is not null
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Validate user is BUSINESS or ADMIN
        if (user.getRole() != Role.ROLE_ADMIN && user.getRole() != Role.ROLE_BUSINESS) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Delete the time slot
        timeSlotService.deleteTimeSlot(restaurantId, timeSlotId, (Business) user);
        
        return ResponseEntity.noContent().build();
    }
    
}
