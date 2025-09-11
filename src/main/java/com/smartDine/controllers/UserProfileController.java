package com.smartDine.controllers;
 // DTO para la actualización
import com.smartDine.entity.Role;
import com.smartDine.entity.User;
import com.smartDine.services.BusinessService; // Asumimos que este servicio ya existe
import com.smartDine.services.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me") // Todas las rutas de este controlador parten de /api/me
public class UserProfileController {

    private final CustomerService customersService;
    private final BusinessService businessService;

    public UserProfileController(CustomerService customersService, BusinessService businessService) {
        this.customersService = customersService;
        this.businessService = businessService;
    }

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
            // Si es un cliente, buscamos su perfil completo en el CustomersService.
            // Es importante buscar por ID para obtener la entidad completa de la subclase.
            return ResponseEntity.ok(
                customersService.getCustomerById(user.getId()) 
            );
        } else if (user.getRole() == Role.ROLE_BUSINESS) {
            // Si es una empresa, hacemos lo propio con el BusinessService.
            return ResponseEntity.ok(
                businessService.getBusinessById(user.getId()) 
            );
        }

        // Si el rol no es ninguno de los esperados, devolvemos un error.
        return ResponseEntity.status(403).body("User has an unrecognized role.");
    }
}
