package com.teammate.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class CredentialManagerTest {
    private CredentialManager cm;

    @BeforeEach
    public void setUp() {
        // Clean file before each test
        new File("player_credentials.csv").delete();
        cm = new CredentialManager();
    }

    // TEST 1: Organizer can login
    @Test
    public void organizerLogin_Works() {
        assertTrue(cm.authenticateOrganizer("admin", "admin123"));
        assertFalse(cm.authenticateOrganizer("wrong", "wrong"));
    }

    // TEST 2: Player registration prevents duplicates
    @Test
    public void playerRegistration_PreventsDuplicates() {
        assertTrue(cm.registerPlayer("user1", "pass1", "test1@iit.ac.lk", "John"));
        assertFalse(cm.registerPlayer("user1", "pass2", "test2@iit.ac.lk", "Jane")); // Same username
        assertFalse(cm.registerPlayer("user2", "pass3", "test1@iit.ac.lk", "Jane")); // Same email
    }

    // TEST 3: Player authentication works
    @Test
    public void playerLogin_Works() {
        cm.registerPlayer("user1", "pass123", "test@iit.ac.lk", "John");
        assertTrue(cm.authenticatePlayer("user1", "pass123"));
        assertFalse(cm.authenticatePlayer("user1", "wrong"));
        assertFalse(cm.authenticatePlayer("nonexistent", "pass123"));
    }

    // TEST 4: Data persistence (saves/loads from file)
    @Test
    public void dataSavesToFile() {
        cm.registerPlayer("user1", "pass1", "test1@iit.ac.lk", "John");
        CredentialManager cm2 = new CredentialManager(); // Loads from file
        assertTrue(cm2.usernameExists("user1"));
    }

    // TEST 5: Basic getters work
    @Test
    public void gettersWork() {
        cm.registerPlayer("user1", "pass1", "john@iit.ac.lk", "John Doe");
        assertEquals("John Doe", cm.getPlayerName("user1"));
        assertEquals("john@iit.ac.lk", cm.getPlayerEmail("user1"));
        assertEquals(1, cm.getPlayerCount());
    }
}