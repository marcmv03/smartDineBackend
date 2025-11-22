package com.smartDine.controllers;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartDine.dto.MenuItemDTO;
import com.smartDine.dto.UploadResponse;
import com.smartDine.entity.Business;
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
        // Validate user is BUSINESS
        if (!(user instanceof Business)) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                "Only business owners can create menu items");
        }
        
        MenuItem menuItem = menuItemService.createMenuItemForRestaurant(restaurantId, menuItemDto, (Business) user);
        MenuItemDTO menuItemDTO = MenuItemDTO.fromEntity(List.of(menuItem)).get(0);
        return new ResponseEntity<>(menuItemDTO, HttpStatus.CREATED);
    }
    
    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<List<MenuItemDTO>> getMenuItemsByRestaurant(@PathVariable Long restaurantId, @AuthenticationPrincipal User user) {
        List<MenuItem> menuItems = menuItemService.getMenuItemsByRestaurant(restaurantId);
        List<MenuItemDTO> menuItemDTOs = MenuItemDTO.fromEntity(menuItems);
        return ResponseEntity.ok(menuItemDTOs);
    }
    @GetMapping("/menu-items/{menuItemId}") 
    public ResponseEntity<MenuItemDTO> getMenuItemById(@PathVariable Long menuItemId, @AuthenticationPrincipal User user) {
        MenuItem menuItem = menuItemService.getMenuItemById(menuItemId);
        MenuItemDTO menuItemDTO = MenuItemDTO.fromEntity(menuItem); 
        return ResponseEntity.ok(menuItemDTO);
    }
    
    /**
     * Uploads an image for a specific menu item.
     * 
     * @param restaurantId the ID of the restaurant
     * @param menuItemId the ID of the menu item
     * @param file the image file to upload
     * @param user the authenticated user (must be the restaurant owner)
     * @return ResponseEntity containing UploadResponse with file details
     * @throws IOException if the file upload fails
     */
    @PostMapping(
        value = "/restaurants/{restaurantId}/menu-items/{menuItemId}/images",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UploadResponse> uploadMenuItemImage(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws IOException {
        
        // Validate file
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Validate user is BUSINESS
        if (!(user instanceof Business)) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                "Only business owners can upload menu item images");
        }
        
        // Delegate upload and assignment to service
        UploadResponse response = menuItemService.uploadMenuItemImage(
            restaurantId, menuItemId, file, (Business) user);
        URI location = URI.create("/api/images/" + response.getKey());
        
        return ResponseEntity.created(location).body(response);
    }

}
