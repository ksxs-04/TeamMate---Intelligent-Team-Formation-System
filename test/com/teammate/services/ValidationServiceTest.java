package com.teammate.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class ValidationServiceTest {

    // TEST 1: Email validation (MOST IMPORTANT - IIT emails only)
    @Test
    public void emailValidation_IITOnly() {
        assertTrue(ValidationService.isValidEmail("test@iit.ac.lk"));
        assertFalse(ValidationService.isValidEmail("test@gmail.com"));
        assertFalse(ValidationService.isValidEmail("invalid"));
    }

    // TEST 2: Game validation
    @Test
    public void gameValidation_ValidGames() {
        assertTrue(ValidationService.isValidGame("Valorant"));
        assertFalse(ValidationService.isValidGame("UnknownGame"));
    }

    // TEST 3: Name validation
    @Test
    public void nameValidation_BasicChecks() {
        List<String> errors = ValidationService.validateName("John Doe");
        assertTrue(errors.isEmpty());

        errors = ValidationService.validateName("");
        assertFalse(errors.isEmpty());
    }
}