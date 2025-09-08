package com.smartDine.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for phone numbers.
 * Validates phone number format, length, and content.
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhone, Long> {

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Long phoneNumber, ConstraintValidatorContext context) {
        // Null values should be handled by @NotNull annotation
        if (phoneNumber == null) {
            return true;
        }

        String phoneStr = phoneNumber.toString();
        
        // Check for minimum and maximum length (7-15 digits)
        if (phoneStr.length() < 7 || phoneStr.length() > 15) {
            return false;
        }
        
        // Check that all characters are digits (no negative numbers)
        if (phoneNumber < 0) {
            return false;
        }
        
        // Additional validation rules:
        // 1. Must not start with 0 (except for specific country codes)
        // 2. Must contain only numeric digits
        // 3. Common Spanish phone number patterns validation
        
        // Spanish mobile numbers typically start with 6, 7, or 9 and have 9 digits
        // Spanish landline numbers typically start with 8 or 9 and have 9 digits
        // International numbers can be longer
        
        if (phoneStr.length() == 9) {
            // Spanish phone number validation
            char firstDigit = phoneStr.charAt(0);
            return firstDigit == '6' || firstDigit == '7' || firstDigit == '8' || firstDigit == '9';
        }
        
        // For international numbers (10-15 digits), allow more flexibility
        // but ensure they don't start with 0
        if (phoneStr.length() >= 10) {
            return phoneStr.charAt(0) != '0';
        }
        
        // For 7-8 digit numbers, allow them but ensure they don't start with 0
        return phoneStr.charAt(0) != '0';
    }
}