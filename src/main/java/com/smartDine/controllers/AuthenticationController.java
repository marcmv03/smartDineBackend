package com.smartDine.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.auth.LoginRequest;
import com.smartDine.dto.auth.RegisterBusinessRequest;
import com.smartDine.dto.auth.RegisterCustomerRequest;
import com.smartDine.entity.User;
import com.smartDine.services.AuthenticationService;
import com.smartDine.services.JwtService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/smartdine/api/auth")
@CrossOrigin(origins = "*")
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody RegisterCustomerRequest registerCustomerDto) {
        try {
            User registeredUser = authService.registerCustomer(registerCustomerDto);
            String jwtToken = jwtService.generateToken(registeredUser);
            
            return ResponseEntity.ok(new LoginResponse(
                jwtToken, 
                jwtService.getExpirationTime(),
                registeredUser.getId(),
                registeredUser.getName(),
                registeredUser.getEmail()
            ));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Ya existe un usuario con este email o número de teléfono")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateCustomer(@Valid @RequestBody LoginRequest loginUserDto) {
        try {
            User authenticatedUser = authService.authenticate(loginUserDto);
            String jwtToken = jwtService.generateToken(authenticatedUser);

            return ResponseEntity.ok(new LoginResponse(
                jwtToken, 
                jwtService.getExpirationTime(),
                authenticatedUser.getId(),
                authenticatedUser.getName(),
                authenticatedUser.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Credenciales inválidas"));
        }
    }

    @PostMapping("/register/business")
    public ResponseEntity<?> registerBusiness(@Valid @RequestBody RegisterBusinessRequest registerRequest) {
        try {
            User registeredUser = authService.registerBusiness(registerRequest);
            String jwtToken = jwtService.generateToken(registeredUser);
            
            return ResponseEntity.ok(new LoginResponse(
                jwtToken, 
                jwtService.getExpirationTime(),
                registeredUser.getId(),
                registeredUser.getName(),
                registeredUser.getEmail()
            ));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Ya existe un usuario con este email o número de teléfono")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }


    // Response classes
    public static class LoginResponse {
        private String token;
        private long expiresIn;
        private Long userId;
        private String name;
        private String email;

        public LoginResponse(String token, long expiresIn, Long userId, String name, String email) {
            this.token = token;
            this.expiresIn = expiresIn;
            this.userId = userId;
            this.name = name;
            this.email = email;
        }

        // Getters
        public String getToken() { return token; }
        public long getExpiresIn() { return expiresIn; }
        public Long getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    public static class ErrorResponse {
        private String message;
        private boolean success = false;

        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return success; }
    }
}
