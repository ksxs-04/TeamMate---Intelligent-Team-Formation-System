package com.teammate.services;

import com.teammate.models.Participant;
import com.teammate.models.Team;
import com.teammate.models.GameRole;
import com.teammate.models.IdGenerator;
import com.teammate.exceptions.FileProcessingException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    public List<Participant> loadParticipants(String filePath) throws FileProcessingException {

        // Create directory if it doesn't exist
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            System.out.println("📁 Created directory: " + parentDir.getAbsolutePath());
        }

        if (!Files.exists(Paths.get(filePath))) {
            throw new FileProcessingException("File not found: " + filePath +
                    "\n💡 Please check the file path and try again.");
        }

        List<Participant> participants = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            int lineNumber = 0;
            int successCount = 0;
            int errorCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1) continue; // Skip header

                if (line.trim().isEmpty()) continue; // Skip empty lines

                try {
                    Participant participant = parseParticipant(line, lineNumber);
                    participants.add(participant);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("❌ Skipping invalid data at line " + lineNumber + ": " + e.getMessage());
                    errorCount++;
                }
            }

            // Initialize ID generator with loaded participants
            IdGenerator.initializeCounter(participants);

            System.out.println("📊 File processing summary:");
            System.out.println("   ✅ Successfully loaded: " + successCount + " participants");
            if (errorCount > 0) {
                System.out.println("   ❌ Skipped due to errors: " + errorCount + " lines");
            }

        } catch (IOException e) {
            throw new FileProcessingException("Error reading file: " + e.getMessage() +
                    "\n💡 Please check file permissions and format.");
        }

        if (participants.isEmpty()) {
            throw new FileProcessingException("No valid participant data found in file." +
                    "\n💡 Please ensure the file has the correct format with 7 columns:" +
                    "\n   ParticipantID,Name,Email,GameInterest,SkillLevel,PreferredRole,PersonalityScore");
        }

        return participants;
    }

    private Participant parseParticipant(String line, int lineNumber) throws Exception {
        String[] fields = line.split(",");
        if (fields.length < 7) {
            throw new Exception("Insufficient data fields. Expected 7, got " + fields.length +
                    "\n   Format: ParticipantID,Name,Email,GameInterest,SkillLevel,PreferredRole,PersonalityScore");
        }

        try {
            String id = fields[0].trim();
            String name = fields[1].trim();
            String email = fields[2].trim();
            String gameInterest = fields[3].trim();
            int skillLevel = Integer.parseInt(fields[4].trim());
            GameRole role = GameRole.valueOf(fields[5].trim().toUpperCase());
            int personalityScore = Integer.parseInt(fields[6].trim());

            // Validate basic data
            if (name.isEmpty()) {
                throw new Exception("Name cannot be empty");
            }

            // Validate email domain
            if (!ValidationService.isValidEmail(email)) {
                throw new Exception("Invalid email domain. Must be @iit.ac.lk: " + email);
            }

            // Validate skill level
            if (skillLevel < 1 || skillLevel > 10) {
                throw new Exception("Skill level must be between 1-10: " + skillLevel);
            }

            // Validate personality score
            if (personalityScore < 50 || personalityScore > 100) {
                throw new Exception("Personality score must be between 50-100: " + personalityScore);
            }

            return new Participant(id, name, email, gameInterest, skillLevel, role, personalityScore);

        } catch (NumberFormatException e) {
            throw new Exception("Invalid number format in skill level or personality score");
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid role. Valid roles: " +
                    java.util.Arrays.toString(GameRole.values()));
        }
    }

    public void saveTeams(List<Team> teams, String filePath) throws FileProcessingException {
        try {
            // Create directory if it doesn't exist
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                System.out.println("📁 Created directory: " + parentDir.getAbsolutePath());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
                // Write header
                writer.write("TeamID,TeamName,MemberCount,AverageSkill,Members,GameInterests");
                writer.newLine();

                for (Team team : teams) {
                    String membersStr = String.join("; ",
                            team.getMembers().stream()
                                    .map(Participant::toString)
                                    .toArray(String[]::new)
                    );

                    String gameInterestsStr = String.join(", ", team.getGameInterests());

                    String line = String.format("%s,%s,%d,%.2f,\"%s\",\"%s\"",
                            team.getTeamId(),
                            team.getTeamName(),
                            team.getSize(),
                            team.getAverageSkill(),
                            membersStr,
                            gameInterestsStr
                    );

                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("✅ Successfully saved " + teams.size() + " teams to: " + filePath);

        } catch (IOException e) {
            throw new FileProcessingException("Error writing to file: " + e.getMessage() +
                    "\n💡 Please check if the directory is writable.");
        }
    }

    // Save player to players.csv (appends if file exists)
    public void savePlayerToSurveyFile(Participant participant) throws FileProcessingException {
        String surveyFilePath = "players.csv"; // Root directory
        saveParticipantToFile(participant, surveyFilePath, true);
    }

    // Generic method to save participant to any file
    private void saveParticipantToFile(Participant participant, String filePath, boolean append) throws FileProcessingException {
        try {
            // Create directory if it doesn't exist
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            boolean fileExists = Files.exists(Paths.get(filePath));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
                if (!fileExists) {
                    // Write header
                    writer.write("ParticipantID,Name,Email,GameInterest,SkillLevel,PreferredRole,PersonalityScore");
                    writer.newLine();
                }

                // Write participant data
                writer.write(String.format("%s,%s,%s,%s,%d,%s,%d",
                        participant.getParticipantId(),
                        participant.getName(),
                        participant.getEmail(),
                        participant.getGameInterest(),
                        participant.getSkillLevel(),
                        participant.getPreferredRole().name(),
                        participant.getPersonalityScore()
                ));
                writer.newLine();
            }

            System.out.println("✅ Participant data saved to: " + filePath);

        } catch (IOException e) {
            throw new FileProcessingException("Error saving participant to " + filePath + ": " + e.getMessage());
        }
    }

    public void saveParticipant(Participant participant, String filePath) {
        try {
            saveParticipantToFile(participant, filePath, true);
        } catch (FileProcessingException e) {
            System.err.println("❌ Error saving participant data: " + e.getMessage());
        }
    }

    // Load players from any CSV file path (for organizer import)
    public List<Participant> loadPlayersFromCSV(String filePath) throws FileProcessingException {
        return loadParticipants(filePath);
    }

    // Check if players.csv exists and has data
    public boolean hasSurveyPlayers() {
        try {
            List<Participant> players = loadPlayersFromCSV("players.csv");
            return !players.isEmpty();
        } catch (FileProcessingException e) {
            return false;
        }
    }

    // Save formed teams to a dedicated file
    public void saveFormedTeams(List<Team> teams) throws FileProcessingException {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String teamsFilePath = "formed_teams_" + timestamp + ".csv"; // More descriptive
        System.out.println("💾 Saving teams to: " + new File(teamsFilePath).getAbsolutePath());
        saveTeams(teams, teamsFilePath);
    }

    // Check if a player already exists in players.csv (to avoid duplicates)
    public boolean playerExistsInSurveyFile(String email) {
        try {
            List<Participant> existingPlayers = loadPlayersFromCSV("players.csv");
            return existingPlayers.stream()
                    .anyMatch(p -> p.getEmail().equalsIgnoreCase(email));
        } catch (FileProcessingException e) {
            return false; // File doesn't exist yet
        }
    }

    // Get only new players (those not already imported)
    public List<Participant> getNewPlayers(List<Participant> importedPlayers, List<Participant> currentParticipants) {
        List<Participant> newPlayers = new ArrayList<>();

        for (Participant imported : importedPlayers) {
            boolean alreadyExists = currentParticipants.stream()
                    .anyMatch(existing -> existing.getEmail().equalsIgnoreCase(imported.getEmail()));

            if (!alreadyExists) {
                newPlayers.add(imported);
            }
        }

        return newPlayers;
    }

    // Check if file exists and is readable
    public boolean isFileAccessible(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.canRead();
    }
}