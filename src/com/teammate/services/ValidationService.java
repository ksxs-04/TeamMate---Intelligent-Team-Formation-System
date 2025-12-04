package com.teammate.services;

import com.teammate.models.Participant;
import com.teammate.models.GameRole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ValidationService {
    public static final List<String> VALID_GAMES = Arrays.asList(
            "Valorant", "Dota", "FIFA", "Basketball", "Badminton",
            "CSGO", "League of Legends", "Overwatch"
    );

    public static String normalizeGameName(String game) {
        if (game == null || game.trim().isEmpty()) {
            return null;
        }

        return VALID_GAMES.stream()
                .filter(g -> g.equalsIgnoreCase(game.trim()))
                .findFirst()
                .orElse(game); // fallback to original if not found (though isValidGame should catch this)
    }

    public static boolean isValidGame(String game) {
        return VALID_GAMES.stream()
                .anyMatch(g -> g.equalsIgnoreCase(game.trim()));
    }


    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@iit\\.ac\\.lk$",
            Pattern.CASE_INSENSITIVE
    );

    public static List<String> validateParticipantData(Participant participant) {
        List<String> errors = new ArrayList<>();

        // Validate ID
        if (participant.getParticipantId() == null || participant.getParticipantId().trim().isEmpty()) {
            errors.add("Participant ID cannot be empty");
        }

        // Validate Name
        String name = participant.getName();
        if (name == null || name.trim().isEmpty() || name.trim().length() < 2 || name.trim().length() > 50) {
            errors.add("Name must be between 2-50 characters");
        }

        // Validate Email
        String email = participant.getEmail();
        if (!isValidEmail(email)) {
            errors.add("Email must be a valid @iit.ac.lk address");
        }

        // Validate Game Interest - WITH NORMALIZATION
        String game = participant.getGameInterest();
        if (game == null || game.trim().isEmpty() || !isValidGame(game)) {
            errors.add("Invalid game interest. Valid games: " + String.join(", ", VALID_GAMES));
        } else {
            // NORMALIZE the game name here
            String normalizedGame = normalizeGameName(game);
            participant.setGameInterest(normalizedGame);
        }

        // Validate Skill Level
        int skillLevel = participant.getSkillLevel();
        if (skillLevel < 1 || skillLevel > 10) {
            errors.add("Skill level must be between 1-10");
        }

        // Validate Personality Score
        int personalityScore = participant.getPersonalityScore();
        if (personalityScore < 50 || personalityScore > 100) {
            errors.add("Personality score must be between 50-100");
        }

        return errors;
    }

    public static List<String> validateTeamSize(int teamSize, int participantCount) {
        List<String> errors = new ArrayList<>();

        if (teamSize <= 0) {
            errors.add("Team size must be positive");
        }
        if (participantCount < teamSize) {
            errors.add("Not enough participants (" + participantCount + ") for team size " + teamSize);
        }

        return errors;
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidGameRole(String role) {
        try {
            GameRole.valueOf(role.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static List<String> validateName(String name) {
        List<String> errors = new ArrayList<>();

        if (name == null || name.trim().isEmpty()) {
            errors.add("Name cannot be empty");
        } else if (name.trim().length() < 2) {
            errors.add("Name must be at least 2 characters long");
        } else if (name.trim().length() > 50) {
            errors.add("Name cannot exceed 50 characters");
        } else if (!name.matches("^[a-zA-Z\\s\\-'.’]+$")) {
            errors.add("Name can only contain letters, spaces, hyphens, and apostrophes");
        }

        return errors;
    }
}