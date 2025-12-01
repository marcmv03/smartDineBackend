package com.smartDine.controllers;
 import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping; // Asumimos que este servicio ya existe
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.ProfileDTO;
import com.smartDine.dto.RestaurantDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.Role;
import com.smartDine.entity.User;
import com.smartDine.services.BusinessService;
import com.smartDine.services.CustomerService;
import com.smartDine.services.RestaurantService;

@RestController
@RequestMapping("/smartdine/api/me") // Todas las rutas de este controlador parten de /api/me
public class UserProfileController {
    @Autowired
    private  CustomerService customersService;
    @Autowired
    private  BusinessService businessService;
    @Autowired
    private  RestaurantService restaurantService;


    /**
     * GET /api/me
     * Retrieves the full profile of the currently authenticated user.
     * It inspects the user's role to return either a Customer or Business object.
     *
     * @param user The UserDetails object injected by Spring Security.
     * @return A ResponseEntity containing the specific user profile (Customer or Business).
     */
    @GetMapping
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal User user) {
        // La anotación @AuthenticationPrincipal nos da el objeto User directamente.
        
        // Verificamos el rol del usuario para determinar qué servicio usar.
        if (user.getRole() == Role.ROLE_CUSTOMER) {
            User result = customersService.getCustomerById(user.getId());
            ProfileDTO customerDTO = ProfileDTO.fromEntity(result);
            return ResponseEntity.ok(customerDTO);
            // Si es un cliente, buscamos su perfil completo en el CustomersService.
            // Es importante buscar por ID para obtener la entidad completa de la subclase.
        } else if (user.getRole() == Role.ROLE_BUSINESS) {
            // Si es una empresa, hacemos lo propio con el BusinessService.
            User result = businessService.getBusinessById(user.getId());
            ProfileDTO businessDTO = ProfileDTO.fromEntity(result);
            return ResponseEntity.ok(businessDTO);
        }
        return ResponseEntity.status(403).body("User has an unrecognized role.");
    }
    @GetMapping("/restaurants")
    public List<RestaurantDTO> getMyRestaurants(@AuthenticationPrincipal User user) {
        if (!(user instanceof Business)) {
            return List.of();
        }
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwner((Business)user) ;
        return RestaurantDTO.fromEntity(restaurants);
    }

}
