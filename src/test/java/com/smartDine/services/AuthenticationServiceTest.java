package com.smartDine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.auth.LoginRequest;
import com.smartDine.dto.auth.RegisterBusinessRequest;
import com.smartDine.dto.auth.RegisterCustomerRequest;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.User;
import com.smartDine.repository.UserRepository;

// Use @SpringBootTest to load the full application context
@SpringBootTest
@ActiveProfiles("test")
// Use @Transactional to roll back database changes after each test
@Transactional
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should register and save customer to the database successfully")
    void registerCustomer_shouldSaveCustomerInDatabase() {
        // Arrange
        RegisterCustomerRequest customerRequest = new RegisterCustomerRequest();
        customerRequest.setName("Integration Test Customer");
        customerRequest.setEmail("integration.customer@test.com");
        customerRequest.setPassword("password123"); // Raw password - let service encode it
        customerRequest.setPhoneNumber(111222333L);

        // Act
        User savedCustomer = authenticationService.registerCustomer(customerRequest);

        // Assert
        assertNotNull(savedCustomer);
        assertNotNull(savedCustomer.getId(), "Saved customer should have an ID");

        // Verify by fetching from the database directly
        User userFromDb = userRepository.findByEmail("integration.customer@test.com").orElse(null);
        
        assertNotNull(userFromDb, "User should exist in the database");
        assertEquals("Integration Test Customer", userFromDb.getName());
        assertTrue(passwordEncoder.matches("password123", userFromDb.getPassword()), "Password should be encoded");
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when saving a customer with a non-unique email")
    void registerCustomer_whenEmailExistsInDb_shouldThrowDataIntegrityViolationException() {
        // Arrange: First, save a user to ensure the email exists in the H2 database.
        Customer existingCustomer = new Customer();
        existingCustomer.setName("Existing User");
        existingCustomer.setEmail("existing.email@test.com");
        existingCustomer.setPassword("anypassword");
        existingCustomer.setPhoneNumber(000000000L);
        userRepository.saveAndFlush(existingCustomer); // Use saveAndFlush to commit to DB immediately
        
        // Now, create a new user entity with the same email, bypassing the service check
        Customer duplicateCustomer = new Customer();
        duplicateCustomer.setName("Another Customer");
        duplicateCustomer.setEmail("existing.email@test.com");
        duplicateCustomer.setPassword("password123");
        duplicateCustomer.setPhoneNumber(111222333L);

        // Act & Assert: Now we expect the database constraint violation exception
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(duplicateCustomer);
        });
    }


    @Test
    @DisplayName("Should register and save business to the database successfully")
    void registerBusiness_shouldSaveBusinessInDatabase() {
        // Arrange
        RegisterBusinessRequest businessRequest = new RegisterBusinessRequest();
        businessRequest.setName("Integration Test Business");
        businessRequest.setEmail("integration.business@test.com");
        businessRequest.setPassword("password456");
        businessRequest.setPhoneNumber(444555666L);

        // Act
        Business savedBusiness = authenticationService.registerBusiness(businessRequest);

        // Assert
        assertNotNull(savedBusiness);
        assertNotNull(savedBusiness.getId(), "Saved business should have an ID");

        User userFromDb = userRepository.findByEmail("integration.business@test.com").orElse(null);
        
        assertNotNull(userFromDb, "User should exist in the database");
        assertEquals("Integration Test Business", userFromDb.getName());
        assertTrue(passwordEncoder.matches("password456", userFromDb.getPassword()), "Password should be encoded");
    }


    @Test
    @DisplayName("Should return the authenticated User for correct credentials")
    void login_withValidCredentials_shouldReturnUser() {
        // Arrange: Create and save a user first
        RegisterCustomerRequest customerRequest = new RegisterCustomerRequest();
        customerRequest.setName("Login Test User");
        customerRequest.setEmail("login.user@test.com");
        customerRequest.setPassword("correct-password");
        customerRequest.setPhoneNumber(123123123L);
        authenticationService.registerCustomer(customerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login.user@test.com");
        loginRequest.setPassword("correct-password");

        // Act: The service now returns the User entity
        User authenticatedUser = authenticationService.authenticate(loginRequest);

        // Assert
        assertNotNull(authenticatedUser);
        assertEquals("login.user@test.com", authenticatedUser.getEmail());
        assertEquals("Login Test User", authenticatedUser.getName());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException for a user with invalid password")
    void login_withInvalidPassword_shouldThrowBadCredentialsException() {
        // Arrange
        RegisterCustomerRequest customerRequest = new RegisterCustomerRequest();
        customerRequest.setName("Login Test User");
        customerRequest.setEmail("login.user.fail@test.com");
        customerRequest.setPassword("correct-password");
        customerRequest.setPhoneNumber(321321321L);
        authenticationService.registerCustomer(customerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login.user.fail@test.com");
        loginRequest.setPassword("wrong-password");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(loginRequest);
        });
    }
}