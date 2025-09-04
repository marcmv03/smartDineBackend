package com.smartDine.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.LoginUserDTO;
import com.smartDine.dto.RegisterUserDTO;
import com.smartDine.entity.User;
import com.smartDine.services.AuthService;
import com.smartDine.services.JwtService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/smartdine/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final JwtService jwtService;
    private final AuthService authService;

    public AuthController(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDTO registerUserDto) {
        try {
            User registeredUser = authService.signup(registerUserDto);
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
    public ResponseEntity<?> authenticate(@Valid @RequestBody LoginUserDTO loginUserDto) {
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
