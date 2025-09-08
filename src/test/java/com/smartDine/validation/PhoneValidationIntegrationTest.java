package com.smartDine.validation;

import com.smartDine.dto.RegisterUserDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify phone validation works in the Spring context.
 */
@SpringBootTest
class PhoneValidationIntegrationTest {

    @Autowired
    private Validator validator;

    @Test
    void whenValidPhoneInSpringContext_thenValidationPasses() {
        RegisterUserDTO validDto = new RegisterUserDTO();
        validDto.setName("Test User");
        validDto.setEmail("test@example.com");
        validDto.setPassword("password123");
        validDto.setPhone(612345678L); // Valid Spanish mobile

        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty(), "Valid phone number should pass validation in Spring context");
    }

    @Test
    void whenInvalidPhoneInSpringContext_thenValidationFails() {
        RegisterUserDTO invalidDto = new RegisterUserDTO();
        invalidDto.setName("Test User");
        invalidDto.setEmail("test@example.com");
        invalidDto.setPassword("password123");
        invalidDto.setPhone(123456L); // Too short

        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty(), "Invalid phone number should fail validation in Spring context");
        
        boolean hasPhoneViolation = violations.stream()
            .anyMatch(v -> "Número de teléfono inválido".equals(v.getMessage()));
        assertTrue(hasPhoneViolation, "Should have phone validation message");
    }
}