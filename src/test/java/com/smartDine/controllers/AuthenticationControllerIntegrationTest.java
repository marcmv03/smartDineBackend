package com.smartDine.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import com.smartDine.dto.auth.LoginRequest;
import com.smartDine.dto.auth.RegisterBusinessRequest;
import com.smartDine.dto.auth.RegisterCustomerRequest;
import com.smartDine.entity.User;
import com.smartDine.services.AuthenticationService;
import com.smartDine.services.JwtService;

public class AuthenticationControllerIntegrationTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationService authService;

    @InjectMocks
    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void registerCustomer_success_shouldReturnTokenAndUser() {
        RegisterCustomerRequest req = new RegisterCustomerRequest();
        req.setName("IT Customer");
        req.setEmail("it.customer@test.com");
        req.setPassword("pass123");
        req.setPhoneNumber(100200300L);

    com.smartDine.entity.Customer fakeUser = new com.smartDine.entity.Customer("IT Customer", "it.customer@test.com", "pass123", 100200300L);
    fakeUser.setId(42L);

        when(authService.registerCustomer(any(RegisterCustomerRequest.class))).thenReturn(fakeUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        ResponseEntity<?> resp = controller.registerCustomer(req);
        assertNotNull(resp);
    assertEquals(200, resp.getStatusCode().value());
    Object body = resp.getBody();
    assertNotNull(body);
    AuthenticationController.LoginResponse lr = (AuthenticationController.LoginResponse) body;
    assertEquals("fake-jwt-token", lr.getToken());
    assertEquals(3600L, lr.getExpiresIn());
    assertEquals(42L, lr.getUserId());
    }

    @Test
    public void registerCustomer_conflict_shouldReturnBadRequest() {
        RegisterCustomerRequest req = new RegisterCustomerRequest();
        req.setName("IT Customer");
        req.setEmail("it.customer@test.com");
        req.setPassword("pass123");
        req.setPhoneNumber(100200300L);

        doThrow(new DataIntegrityViolationException("duplicate")).when(authService).registerCustomer(any(RegisterCustomerRequest.class));

        ResponseEntity<?> resp = controller.registerCustomer(req);
        assertNotNull(resp);
    assertEquals(400, resp.getStatusCode().value());
    Object body = resp.getBody();
    assertNotNull(body);
    AuthenticationController.ErrorResponse er = (AuthenticationController.ErrorResponse) body;
    assertNotNull(er.getMessage());
    assertEquals("Ya existe un usuario con este email o número de teléfono", er.getMessage());
    }

    @Test
    public void login_success_shouldReturnToken() {
        LoginRequest login = new LoginRequest();
        login.setEmail("it.customer@test.com");
        login.setPassword("pass123");

    com.smartDine.entity.Customer fakeUser = new com.smartDine.entity.Customer("IT Customer", "it.customer@test.com", "pass123", 100200300L);
    fakeUser.setId(43L);

        when(authService.authenticate(any(LoginRequest.class))).thenReturn(fakeUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-login-token");
        when(jwtService.getExpirationTime()).thenReturn(7200L);

        ResponseEntity<?> resp = controller.authenticateCustomer(login);
        assertNotNull(resp);
    assertEquals(200, resp.getStatusCode().value());
    Object body = resp.getBody();
    assertNotNull(body);
    AuthenticationController.LoginResponse lr = (AuthenticationController.LoginResponse) body;
    assertEquals("jwt-login-token", lr.getToken());
    }

    @Test
    public void registerBusiness_success_shouldReturnToken() {
        RegisterBusinessRequest req = new RegisterBusinessRequest();
        req.setName("IT Business");
        req.setEmail("it.business@test.com");
        req.setPassword("bizpass");
        req.setPhoneNumber(400500600L);

    com.smartDine.entity.Business fakeUser = new com.smartDine.entity.Business("IT Business", "it.business@test.com", "bizpass", 400500600L);
    fakeUser.setId(44L);

        when(authService.registerBusiness(any(RegisterBusinessRequest.class))).thenReturn(fakeUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-biz-token");
        when(jwtService.getExpirationTime()).thenReturn(7200L);

        ResponseEntity<?> resp = controller.registerBusiness(req);
        assertNotNull(resp);
    assertEquals(200, resp.getStatusCode().value());
    Object body = resp.getBody();
    assertNotNull(body);
    AuthenticationController.LoginResponse lr = (AuthenticationController.LoginResponse) body;
    assertEquals("jwt-biz-token", lr.getToken());
    }
}
