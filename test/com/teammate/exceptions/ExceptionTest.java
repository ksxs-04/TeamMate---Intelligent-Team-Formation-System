package com.teammate.exceptions;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ExceptionTest {

    @Test
    @DisplayName("Test TeamFormationException")
    public void testTeamFormationException() {
        System.out.println("🧪 Testing TeamFormationException...");

        // Test 1: Basic exception
        String errorMessage = "Not enough participants for team formation";
        TeamFormationException exception = new TeamFormationException(errorMessage);

        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
        System.out.println("  ✓ Basic exception created with message: " + errorMessage);

        // Test 2: Exception with cause
        Exception cause = new IllegalArgumentException("Invalid team size");
        TeamFormationException exceptionWithCause = new TeamFormationException(errorMessage, cause);

        assertEquals(errorMessage, exceptionWithCause.getMessage());
        assertSame(cause, exceptionWithCause.getCause());
        System.out.println("  ✓ Exception with cause: " + cause.getMessage());

        // Test 3: Throw and catch the exception
        try {
            throw new TeamFormationException("Test throw");
        } catch (TeamFormationException e) {
            assertEquals("Test throw", e.getMessage());
            System.out.println("  ✓ Exception thrown and caught successfully");
        }
    }

    @Test
    @DisplayName("Test FileProcessingException")
    public void testFileProcessingException() {
        System.out.println("🧪 Testing FileProcessingException...");

        // Test file-related error messages
        String[] fileErrors = {
                "File not found: participants.csv",
                "Invalid CSV format at line 5",
                "Cannot write to file: permission denied",
                "File is empty or corrupted"
        };

        for (String error : fileErrors) {
            FileProcessingException exception = new FileProcessingException(error);
            assertEquals(error, exception.getMessage());
            System.out.println("  ✓ " + error);
        }

        // Test with IO exception cause
        Exception ioException = new java.io.IOException("Disk full");
        FileProcessingException wrappedException = new FileProcessingException(
                "Failed to save participants", ioException
        );

        assertEquals("Failed to save participants", wrappedException.getMessage());
        assertTrue(wrappedException.getCause() instanceof java.io.IOException);
        System.out.println("  ✓ Wraps IOException correctly");
    }

    @Test
    @DisplayName("Test InvalidNameException")
    public void testInvalidNameException() {
        System.out.println("🧪 Testing InvalidNameException...");

        // Test name validation errors
        String[] nameErrors = {
                "Name cannot be empty",
                "Name contains invalid characters",
                "Name must be between 2-50 characters",
                "Name 'John123' contains numbers"
        };

        for (String error : nameErrors) {
            InvalidNameException exception = new InvalidNameException(error);
            assertEquals(error, exception.getMessage());
            System.out.println("  ✓ " + error);
        }

        // Test with validation cause
        Exception validationError = new IllegalArgumentException("Invalid character: @");
        InvalidNameException exceptionWithCause = new InvalidNameException(
                "Invalid name format", validationError
        );

        assertEquals("Invalid name format", exceptionWithCause.getMessage());
        assertEquals("Invalid character: @", exceptionWithCause.getCause().getMessage());
        System.out.println("  ✓ Exception with validation cause");
    }

    @Test
    @DisplayName("Test InvalidDataException")
    public void testInvalidDataException() {
        System.out.println("🧪 Testing InvalidDataException...");

        // Test various data validation errors
        Object[][] dataErrors = {
                {"Invalid email: test@gmail.com", "Email must be @iit.ac.lk"},
                {"Skill level 15 is invalid", "Must be 1-10"},
                {"Personality score 120 is invalid", "Must be 50-100"},
                {"Invalid game: Fortnite", "Game not in valid list"}
        };

        for (Object[] error : dataErrors) {
            String message = (String) error[0];
            String expectedDetail = (String) error[1];

            InvalidDataException exception = new InvalidDataException(message);
            assertEquals(message, exception.getMessage());
            System.out.println("  ✓ " + message + " (" + expectedDetail + ")");
        }

        // Test with number format exception
        Exception numberError = new NumberFormatException("For input string: \"abc\"");
        InvalidDataException dataException = new InvalidDataException(
                "Invalid number in CSV", numberError
        );

        assertEquals("Invalid number in CSV", dataException.getMessage());
        assertTrue(dataException.getCause() instanceof NumberFormatException);
        System.out.println("  ✓ Wraps NumberFormatException for invalid data");
    }

    @Test
    @DisplayName("Test Exception Hierarchy and Usage")
    public void testExceptionHierarchy() {
        System.out.println("🧪 Testing Exception Hierarchy...");

        // Verify all are Exception subclasses
        assertTrue(new TeamFormationException("test") instanceof Exception);
        assertTrue(new FileProcessingException("test") instanceof Exception);
        assertTrue(new InvalidNameException("test") instanceof Exception);
        assertTrue(new InvalidDataException("test") instanceof Exception);

        System.out.println("  ✓ All custom exceptions extend Exception class");

        // Test polymorphism - can be caught as general Exception
        Exception[] exceptions = {
                new TeamFormationException("Team error"),
                new FileProcessingException("File error"),
                new InvalidNameException("Name error"),
                new InvalidDataException("Data error")
        };

        for (Exception ex : exceptions) {
            try {
                throw ex;
            } catch (Exception e) {
                // All should be catchable as Exception
                assertNotNull(e.getMessage());
                System.out.println("  ✓ Caught as Exception: " + e.getClass().getSimpleName());
            }
        }
    }



    // Helper methods to simulate real scenarios
    private void simulateFileImport(String filename) throws FileProcessingException {
        if (!filename.equals("participants.csv")) {
            throw new FileProcessingException("File not found: " + filename);
        }
    }

    private void simulateTeamFormation(int teamSize, int participantCount)
            throws TeamFormationException {
        if (participantCount < teamSize) {
            throw new TeamFormationException(
                    "Not enough participants (" + participantCount +
                            ") for team size " + teamSize
            );
        }
    }

    private void simulateParticipantCreation(String name, String email)
            throws InvalidNameException, InvalidDataException {
        // Simulate name validation
        if (name.matches(".*\\d.*")) {
            throw new InvalidNameException("Name contains numbers: " + name);
        }

        // Simulate email validation
        if (!email.endsWith("@iit.ac.lk")) {
            throw new InvalidDataException("Invalid email domain: " + email);
        }
    }
}