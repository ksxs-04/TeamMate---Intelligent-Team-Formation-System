package com.teammate.services;

import com.teammate.models.Participant;
import com.teammate.models.GameRole;
import com.teammate.models.PersonalityType;
import com.teammate.models.IdGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SurveyProcessor {
    private static final List<String> PERSONALITY_QUESTIONS = Arrays.asList(
            "I prefer taking charge in group situations (1-5): ",
            "I enjoy analyzing problems before taking action (1-5): ",
            "I work well under pressure (1-5): ",
            "I prefer following established procedures (1-5): ",
            "I enjoy coming up with creative solutions (1-5): "
    );

    public Participant conductInteractiveSurvey(String playerEmail, String playerName, Scanner scanner) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("        PERSONALITY SURVEY");
        System.out.println("=".repeat(50));
        System.out.println("Player: " + playerName);
        System.out.println("Email: " + playerEmail);
        System.out.println("Please complete the following survey:");

        try {
            // Game interest
            String gameInterest = "";
            while (gameInterest.trim().isEmpty()) {
                System.out.println("\nAvailable games:");
                for (String g : ValidationService.VALID_GAMES) {
                    System.out.println(" - " + g);
                }

                System.out.print("Enter your preferred game: ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Game interest cannot be empty.");
                    continue;
                }

                if (!ValidationService.isValidGame(input)) {
                    System.out.println("Invalid game interest. Please choose from the list above.");
                    continue;
                }

                // NORMALIZE the game name to match VALID_GAMES case
                gameInterest = ValidationService.normalizeGameName(input);
                System.out.println("Selected: " + gameInterest);
            }

            // Rest of your code remains the same...
            // Skill level with validation
            int skillLevel = 0;
            while (skillLevel < 1 || skillLevel > 10) {
                try {
                    System.out.print("Enter your Skill Level (1-10): ");
                    skillLevel = Integer.parseInt(scanner.nextLine().trim());
                    if (skillLevel < 1 || skillLevel > 10) {
                        System.out.println("Please enter a number between 1 and 10.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                }
            }

            // Role selection
            GameRole role = null;
            while (role == null) {
                System.out.println("\nAvailable Roles:");
                for (GameRole gameRole : GameRole.values()) {
                    System.out.println("  - " + gameRole.name() + ": " + gameRole.getDisplayName());
                }

                System.out.print("Enter your Preferred Role: ");
                String roleInput = scanner.nextLine().trim().toUpperCase();
                try {
                    role = GameRole.valueOf(roleInput);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid role. Please choose from: " +
                            String.join(", ", Arrays.stream(GameRole.values())
                                    .map(GameRole::name)
                                    .toArray(String[]::new)));
                }
            }

            // Personality assessment
            System.out.println("\n" + "=".repeat(50));
            System.out.println("      PERSONALITY ASSESSMENT");
            System.out.println("=".repeat(50));
            System.out.println("Please rate the following statements from 1 (Strongly Disagree) to 5 (Strongly Agree):");

            int totalScore = 0;
            for (int i = 0; i < PERSONALITY_QUESTIONS.size(); i++) {
                int response = 0;
                while (response < 1 || response > 5) {
                    try {
                        System.out.print((i + 1) + ". " + PERSONALITY_QUESTIONS.get(i));
                        response = Integer.parseInt(scanner.nextLine().trim());
                        if (response < 1 || response > 5) {
                            System.out.println("Please enter a number between 1 and 5.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number.");
                    }
                }
                totalScore += response;
            }

            // Convert to 50-100 scale
            int personalityScore = 50 + (totalScore - 5) * 2;

            PersonalityType personalityType = PersonalityType.fromScore(personalityScore);
            System.out.println("\nYour personality score: " + personalityScore + "/100 (" + personalityType.getDisplayName() + ")");

            // Create participant - ID will be auto-generated by constructor
            Participant participant = new Participant(
                    playerName,
                    playerEmail,
                    gameInterest, // This now contains the normalized game name
                    skillLevel,
                    role,
                    personalityScore
            );

            // Validate participant data
            List<String> errors = ValidationService.validateParticipantData(participant);
            if (!errors.isEmpty()) {
                System.out.println("Validation errors:");
                for (String error : errors) {
                    System.out.println("  - " + error);
                }
                return null;
            }

            System.out.println("\nSurvey completed successfully!");
            System.out.println("Your Participant ID: " + participant.getParticipantId());
            System.out.println("Welcome to the gaming club, " + playerName + "!");
            return participant;

        } catch (Exception e) {
            System.out.println("Unexpected error in survey: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getPersonalityQuestions() {
        return List.copyOf(PERSONALITY_QUESTIONS);
    }
}