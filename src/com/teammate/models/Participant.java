package com.teammate.models;

import com.teammate.services.ValidationService; // Add this import
import java.util.concurrent.atomic.AtomicInteger;

public class Participant {
    private static final AtomicInteger idCounter = new AtomicInteger(1);

    private String participantId;
    private String name;
    private String email;
    private String gameInterest;
    private int skillLevel;
    private GameRole preferredRole;
    private int personalityScore;
    private PersonalityType personalityType;

    // Main constructor - AUTO-GENERATES ID
    public Participant(String name, String email, String gameInterest,
                       int skillLevel, GameRole preferredRole, int personalityScore) {
        this.participantId = IdGenerator.generateParticipantId();
        this.name = name;
        this.email = email;
        // NORMALIZE game interest
        setGameInterest(gameInterest);
        this.skillLevel = skillLevel;
        this.preferredRole = preferredRole;
        setPersonalityScore(personalityScore);
    }

    // Special constructor for loading existing participants (from CSV)
    public Participant(String participantId, String name, String email, String gameInterest,
                       int skillLevel, GameRole preferredRole, int personalityScore) {
        this.participantId = participantId;
        this.name = name;
        this.email = email;
        this.gameInterest = ValidationService.normalizeGameName(gameInterest);
        this.skillLevel = skillLevel;
        this.preferredRole = preferredRole;
        setPersonalityScore(personalityScore);

        // Update the counter to avoid ID conflicts
        IdGenerator.updateCounter(participantId);
    }

    // Proper setter for game interest with normalization
    public void setGameInterest(String gameInterest) {
        if (gameInterest != null) {
            // Normalize the game name to match VALID_GAMES case
            this.gameInterest = ValidationService.normalizeGameName(gameInterest);
        } else {
            this.gameInterest = null;
        }
    }

    // Special setter for personality score
    public void setPersonalityScore(int score) {
        if (score < 50 || score > 100) {
            throw new IllegalArgumentException("Personality score must be between 50-100");
        }
        this.personalityScore = score;
        this.personalityType = PersonalityType.fromScore(score);
    }

    // Getters
    public String getParticipantId() { return participantId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getGameInterest() { return gameInterest; }
    public int getSkillLevel() { return skillLevel; }
    public GameRole getPreferredRole() { return preferredRole; }
    public int getPersonalityScore() { return personalityScore; }
    public PersonalityType getPersonalityType() { return personalityType; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s | Email: %s | Role: %s | Personality: %s",
                name, participantId, gameInterest, email, preferredRole, personalityType);
    }
}