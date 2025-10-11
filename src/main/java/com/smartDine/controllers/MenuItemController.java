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
    public ResponseEntity<?> createMenuItem(@PathVariable Long restaurantId, @Valid @RequestBody MenuItemDTO menuItemDto, @AuthenticationPrincipal User user) {
        MenuItem menuItem = menuItemService.createMenuItemForRestaurant(restaurantId, menuItemDto, user);
        return new ResponseEntity<>(menuItem, HttpStatus.CREATED);
    }
    //to-do
    //make a get mapping to get the menu items.
    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<?> getMenuItemsByRestaurant(@PathVariable Long restaurantId, @AuthenticationPrincipal User user) {
        List<MenuItem> menuItems = menuItemService.getMenuItemsByRestaurant(restaurantId);
        return ResponseEntity.ok(menuItems);
    }
    
}
