package com.smartDine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.auth.LoginRequest;
import com.smartDine.dto.auth.LoginResponse;
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
    public ResponseEntity<LoginResponse> registerCustomer(@Valid @RequestBody RegisterCustomerRequest registerCustomerDto) {
        User registeredUser = authService.registerCustomer(registerCustomerDto);
        String jwtToken = jwtService.generateToken(registeredUser);
        LoginResponse req = LoginResponse.fromEntity( registeredUser);
        req.setToken(jwtToken);
        req.setExpiresIn(jwtService.getExpirationTime());
        return ResponseEntity.ok(req);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateCustomer(@Valid @RequestBody LoginRequest loginUserDto) {
        User authenticatedUser = authService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse req = LoginResponse.fromEntity( authenticatedUser);
        req.setToken(jwtToken);
        req.setExpiresIn(jwtService.getExpirationTime());
        return ResponseEntity.ok(req) ;
    }

    @PostMapping("/register/business")
    public ResponseEntity<LoginResponse> registerBusiness(@Valid @RequestBody RegisterBusinessRequest registerRequest) {
        User registeredUser = authService.registerBusiness(registerRequest);
        String jwtToken = jwtService.generateToken(registeredUser);
        LoginResponse req = LoginResponse.fromEntity( registeredUser);
        req.setToken(jwtToken);
        req.setExpiresIn(jwtService.getExpirationTime());
        return ResponseEntity.ok(req);
    }


}
