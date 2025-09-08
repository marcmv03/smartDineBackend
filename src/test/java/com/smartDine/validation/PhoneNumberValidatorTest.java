package com.smartDine.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartDine.dto.RegisterUserDTO;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for phone number validation.
 */
class PhoneNumberValidatorTest {

    private Validator validator;
    private RegisterUserDTO dto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        dto = new RegisterUserDTO();
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
    }

    @Test
    void whenValidSpanishMobileNumber_thenValidationPasses() {
        // Spanish mobile numbers starting with 6, 7, or 9
        dto.setPhone(612345678L);  // Starts with 6
        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid Spanish mobile number should pass validation");

        dto.setPhone(712345678L);  // Starts with 7
        violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid Spanish mobile number should pass validation");

        dto.setPhone(912345678L);  // Starts with 9
        violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid Spanish mobile number should pass validation");
    }

    @Test
    void whenValidSpanishLandlineNumber_thenValidationPasses() {
        // Spanish landline numbers starting with 8 or 9
        dto.setPhone(812345678L);  // Starts with 8
        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid Spanish landline number should pass validation");

        dto.setPhone(912345678L);  // Starts with 9
        violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid Spanish landline number should pass validation");
    }

    @Test
    void whenValidInternationalNumber_thenValidationPasses() {
        // International numbers (10-15 digits, not starting with 0)
        dto.setPhone(1234567890L);     // 10 digits
        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid international number should pass validation");

        dto.setPhone(123456789012345L); // 15 digits
        violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid international number should pass validation");
    }

    @Test
    void whenPhoneNumberTooShort_thenValidationFails() {
        dto.setPhone(123456L);  // Only 6 digits
        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Phone number with less than 7 digits should fail validation");
        
        ConstraintViolation<RegisterUserDTO> violation = violations.iterator().next();
        assertEquals("Número de teléfono inválido", violation.getMessage());
    }

    @Test
    void whenPhoneNumberTooLong_thenValidationFails() {
        dto.setPhone(1234567890123456L);  // 16 digits
        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Phone number with more than 15 digits should fail validation");
        
        ConstraintViolation<RegisterUserDTO> violation = violations.iterator().next();
        assertEquals("Número de teléfono inválido", violation.getMessage());
    }

    @Test
    void whenValidatorTestedDirectly_thenCorrectValidation() {
        PhoneNumberValidator phoneValidator = new PhoneNumberValidator();
        
        // Test valid Spanish numbers
        assertTrue(phoneValidator.isValid(612345678L, null), "Valid Spanish mobile should pass");
        assertTrue(phoneValidator.isValid(912345678L, null), "Valid Spanish number should pass");
        
        // Test invalid Spanish numbers (9 digits but wrong starting digit)
        assertFalse(phoneValidator.isValid(123456789L, null), "Invalid Spanish number should fail");
        assertFalse(phoneValidator.isValid(512345678L, null), "Invalid Spanish number should fail");
        
        // Test valid international numbers
        assertTrue(phoneValidator.isValid(1234567890L, null), "Valid international number should pass");
        
        // Test too short numbers
        assertFalse(phoneValidator.isValid(123456L, null), "Too short number should fail");
        
        // Test too long numbers  
        assertFalse(phoneValidator.isValid(1234567890123456L, null), "Too long number should fail");
        
        // Test null (should pass as @NotNull handles this)
        assertTrue(phoneValidator.isValid(null, null), "Null should pass in validator");
    }

    @Test
    void whenInvalidSpanishNumber_thenValidationFails() {
        // 9-digit number starting with invalid digit (like 1, 2, 3, 4, 5)
        dto.setPhone(123456789L);
        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Spanish number starting with invalid digit should fail validation");
        
        dto.setPhone(512345678L);
        violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Spanish number starting with invalid digit should fail validation");
    }

    @Test
    void whenPhoneNumberIsNull_thenValidationFailsForNotNull() {
        dto.setPhone(null);
        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Null phone number should fail @NotNull validation");
        
        // Should have violation for @NotNull, not @ValidPhone
        boolean hasNotNullViolation = violations.stream()
            .anyMatch(v -> "El número de teléfono es obligatorio".equals(v.getMessage()));
        assertTrue(hasNotNullViolation, "Should have @NotNull validation message");
    }

    @Test
    void whenValidSevenDigitNumber_thenValidationPasses() {
        dto.setPhone(1234567L);  // 7 digits, doesn't start with 0
        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid 7-digit number should pass validation");
    }

    @Test
    void whenValidEightDigitNumber_thenValidationPasses() {
        dto.setPhone(12345678L);  // 8 digits, doesn't start with 0
        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid 8-digit number should pass validation");
    }
}