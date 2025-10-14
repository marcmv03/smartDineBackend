package com.smartDine.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.MenuItemDTO;
import com.smartDine.entity.MenuItem;
import com.smartDine.entity.User;
import com.smartDine.services.MenuItemService;

import jakarta.validation.Valid;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/smartdine/api")
public class MenuItemController {
    @Autowired
    private MenuItemService menuItemService;

    @PostMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<MenuItemDTO> createMenuItem(@PathVariable Long restaurantId, @Valid @RequestBody MenuItemDTO menuItemDto, @AuthenticationPrincipal User user) {
        MenuItem menuItem = menuItemService.createMenuItemForRestaurant(restaurantId, menuItemDto, user);
        MenuItemDTO menuItemDTO = MenuItemDTO.fromEntity(List.of(menuItem)).get(0);
        return new ResponseEntity<>(menuItemDTO, HttpStatus.CREATED);
    }
    
    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<List<MenuItemDTO>> getMenuItemsByRestaurant(@PathVariable Long restaurantId, @AuthenticationPrincipal User user) {
        List<MenuItem> menuItems = menuItemService.getMenuItemsByRestaurant(restaurantId);
        List<MenuItemDTO> menuItemDTOs = MenuItemDTO.fromEntity(menuItems);
        return ResponseEntity.ok(menuItemDTOs);
    }
    
}
