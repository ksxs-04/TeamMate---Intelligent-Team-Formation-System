package com.teammate.services;

import com.teammate.models.Participant;
import com.teammate.models.GameRole;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FileHandlerTest {

    // TEST 1: Can load participants from CSV
    @Test
    public void canLoadFromCSV() throws Exception {
        // Create a test CSV file
        String testCSV = "test_players.csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(testCSV))) {
            writer.println("ParticipantID,Name,Email,GameInterest,SkillLevel,PreferredRole,PersonalityScore");
            writer.println("P0001,John Doe,john@iit.ac.lk,Valorant,7,ATTACKER,85");
        }

        FileHandler fh = new FileHandler();
        List<Participant> participants = fh.loadParticipants(testCSV);

        assertFalse(participants.isEmpty());
        assertEquals("John Doe", participants.get(0).getName());

        new File(testCSV).delete();
    }

    // TEST 2: Handles invalid CSV gracefully
    @Test
    public void handlesInvalidCSV() {
        FileHandler fh = new FileHandler();
        assertThrows(Exception.class, () -> {
            fh.loadParticipants("nonexistent.csv");
        });
    }

    // TEST 3: Can save participant
    @Test
    public void canSaveParticipant() {
        Participant p = new Participant("Jane", "jane@iit.ac.lk", "Dota", 8, GameRole.STRATEGIST, 90);
        FileHandler fh = new FileHandler();

        // Should not crash
        fh.saveParticipant(p, "test_save.csv");
        new File("test_save.csv").delete();
    }


}