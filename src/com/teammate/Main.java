package com.teammate;

import com.teammate.auth.UserManager;
import com.teammate.models.*;
import com.teammate.services.FileHandler;
import com.teammate.services.SurveyProcessor;
import com.teammate.services.TeamBuilder;
import com.teammate.services.ValidationService;
import com.teammate.exceptions.FileProcessingException;
import com.teammate.exceptions.TeamFormationException;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import static com.teammate.services.ValidationService.VALID_GAMES;

public class Main {
    private static UserManager userManager;
    private static FileHandler fileHandler;
    private static SurveyProcessor surveyProcessor;
    private static Scanner scanner;

    private static List<Participant> participants;
    private static List<Team> teams;

    public static void main(String[] args) {
        initializeComponents();
        runApplication();
    }

    private static void initializeComponents() {
        userManager = new UserManager();
        fileHandler = new FileHandler();
        surveyProcessor = new SurveyProcessor();
        scanner = new Scanner(System.in);
        participants = new java.util.ArrayList<>();
        teams = new java.util.ArrayList<>();

        System.out.println("                   TEAMMATE SYSTEM v2.0                       ");
        System.out.println("              Intelligent Team Formation System               ");

        // Initialize ID generator
        IdGenerator.initializeCounter(participants);
    }

    private static void runApplication() {
        boolean running = true;

        while (running) {
            if (!userManager.isLoggedIn()) {
                running = showLoginMenu();
            } else {
                if (userManager.isOrganizer()) {
                    running = showOrganizerMenu();
                } else {
                    running = showPlayerMenu(); // Same menu for both registered and signed-in players
                }
            }
        }

        scanner.close();
        System.out.println("\nThank you for using TeamMate System! Goodbye! 👋");
    }

    private static boolean showLoginMenu() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("                        LOGIN MENU");
        System.out.println("═".repeat(60));
        System.out.println("1. Organizer Login");
        System.out.println("2. Player Access");
        System.out.println("3. System Information");
        System.out.println("4. Exit System");
        System.out.println("═".repeat(60));
        System.out.print("Choose an option (1-4): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    return handleOrganizerLogin();
                case 2:
                    return handlePlayerAccess();
                case 3:
                    showSystemInfo();
                    return true;
                case 4:
                    return false;
                default:
                    System.out.println("❌ Invalid choice. Please enter 1, 2, 3, or 4.");
                    return true;
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Please enter a valid number.");
            return true;
        }
    }

    private static void showSystemInfo() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("                   SYSTEM INFORMATION");
        System.out.println("═".repeat(60));
        System.out.println("📊 Current Statistics:");
        System.out.println("   • Participants loaded: " + participants.size());
        System.out.println("   • Teams formed: " + teams.size());

        // Calculate next ID from file (most accurate)
        int maxId = getMaxIdFromPlayersCSV();
        System.out.println("   • Next Participant ID: P" + String.format("%04d", maxId + 1));

        System.out.println("\n🔐 Login Information:");
        System.out.println("   • Players: Register with IIT email");
        System.out.println("\n💡 Features:");
        System.out.println("   • Player registration with username/password");
        System.out.println("   • Personality-based team formation");
        System.out.println("   • Survey system for player preferences");
        System.out.println("═".repeat(60));
    }

    private static int getMaxIdFromPlayersCSV() {
        int maxId = 0;
        try {
            java.nio.file.Path playersFile = java.nio.file.Paths.get("players.csv");
            if (java.nio.file.Files.exists(playersFile)) {
                List<String> lines = java.nio.file.Files.readAllLines(playersFile);
                for (int i = 1; i < lines.size(); i++) { // Skip header
                    String line = lines.get(i).trim();
                    if (!line.isEmpty()) {
                        String[] parts = line.split(",");
                        if (parts.length > 0) {
                            String id = parts[0].trim();
                            if (id.startsWith("P")) {
                                try {
                                    int idNum = Integer.parseInt(id.substring(1));
                                    if (idNum > maxId) {
                                        maxId = idNum;
                                    }
                                } catch (NumberFormatException e) {
                                    // Skip invalid IDs
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // File doesn't exist or can't be read
        }
        return maxId;
    }

    private static boolean handleOrganizerLogin() {
        if (userManager.organizerLogin(scanner)) {
            System.out.println("✅ Login successful! Welcome, Organizer!");
            return true;
        } else {
            System.out.println("❌ Login failed. Please try again.");
            return true;
        }
    }

    // 🎯 UPDATED: PLAYER ACCESS HANDLER
    private static boolean handlePlayerAccess() {
        if (userManager.playerAccess(scanner)) {
            // After successful registration OR sign in, check if survey is needed
            String userEmail = userManager.getCurrentUserEmail();
            String userName = userManager.getCurrentUserName();

            if (!hasCompletedSurvey(userEmail)) {
                System.out.println("\n📝 Let's complete your personality survey!");
                return handlePlayerSurvey(userEmail, userName);
            } else {
                System.out.println("✅ Welcome back, " + userName + "!");
                System.out.println("💡 You can update your survey if needed.");
                return true;
            }
        }
        return true;
    }

    // 🎯 CHECK IF SURVEY COMPLETED
    private static boolean hasCompletedSurvey(String email) {
        // Check if player already completed survey in players.csv
        try {
            List<Participant> surveyPlayers = fileHandler.loadPlayersFromCSV("players.csv");
            return surveyPlayers.stream()
                    .anyMatch(p -> p.getEmail().equalsIgnoreCase(email));
        } catch (FileProcessingException e) {
            return false;
        }
    }

    // 🎯 PLAYER SURVEY
    private static boolean handlePlayerSurvey(String email, String name) {
        System.out.println("\n🎯 Let's complete your gaming profile survey!");

        Participant newParticipant = surveyProcessor.conductInteractiveSurvey(email, name, scanner);

        if (newParticipant != null) {
            try {
                // Check if player already exists in players.csv (in case of duplicate)
                if (fileHandler.playerExistsInSurveyFile(email)) {
                    System.out.println("ℹ️  Updating your existing survey data...");
                }

                // Save to players.csv
                fileHandler.savePlayerToSurveyFile(newParticipant);
                System.out.println("✅ Survey completed successfully!");
                System.out.println("📝 Your gaming profile has been saved.");
                System.out.println("🎮 You can now view teams and your profile!");
                return true;

            } catch (FileProcessingException e) {
                System.out.println("❌ Error saving survey: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Survey failed. Please try again later.");
        }
        userManager.logout();
        return true;
    }

    private static boolean showOrganizerMenu() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("                   ORGANIZER DASHBOARD");
        System.out.println("═".repeat(60));
        System.out.println("1. Import Players from CSV File");
        System.out.println("2. View All Participants (" + participants.size() + " loaded)");
        System.out.println("3. Form Teams Automatically");
        System.out.println("4. View Formed Teams");
        System.out.println("5. Save Teams to Formed Teams File");
        System.out.println("6. Team Formation Analysis");
        System.out.println("7. Check for New Survey Players");
        System.out.println("8. Backup System Data");
        System.out.println("9. Logout");
        System.out.println("10. Exit System");
        System.out.println("═".repeat(60));
        System.out.print("Choose an option (1-10): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    importPlayersFromCSV();
                    break;
                case 2:
                    viewParticipants();
                    break;
                case 3:
                    formTeams();
                    break;
                case 4:
                    viewTeams();
                    break;
                case 5:
                    saveTeamsToFormedFile();
                    break;
                case 6:
                    showTeamAnalysis();
                    break;
                case 7:
                    checkForNewSurveyPlayers();
                    break;
                case 8:
                    backupSystemData();
                    break;
                case 9:
                    userManager.logout();
                    System.out.println("✅ Logged out successfully.");
                    break;
                case 10:
                    return false;
                default:
                    System.out.println("❌ Invalid choice. Please enter 1-10.");
            }
            return true;

        } catch (NumberFormatException e) {
            System.out.println("❌ Please enter a valid number.");
            return true;
        }
    }

    private static void backupSystemData() {
        try {
            System.out.println("\n💾 BACKUP SYSTEM DATA");
            System.out.println("─".repeat(40));

            // Create timestamp for backup files
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            // Backup players.csv to root directory
            String playersFile = "players.csv";
            String playersBackup = "players_backup_" + timestamp + ".csv";

            if (java.nio.file.Files.exists(java.nio.file.Paths.get(playersFile))) {
                java.nio.file.Files.copy(
                        java.nio.file.Paths.get(playersFile),
                        java.nio.file.Paths.get(playersBackup),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                System.out.println("✅ Players data backed up to: " + playersBackup);
            } else {
                System.out.println("⚠️  players.csv not found - skipping");
            }

            // Backup player credentials
            String credentialsFile = "player_credentials.csv";
            String credentialsBackup = "player_credentials_backup_" + timestamp + ".csv";

            if (java.nio.file.Files.exists(java.nio.file.Paths.get(credentialsFile))) {
                java.nio.file.Files.copy(
                        java.nio.file.Paths.get(credentialsFile),
                        java.nio.file.Paths.get(credentialsBackup),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                System.out.println("✅ Player credentials backed up to: " + credentialsBackup);
            } else {
                System.out.println("⚠️  player_credentials.csv not found - skipping");
            }

            // Backup any formed teams files - FIXED VERSION
            try {
                System.out.println("\n🔍 Looking for teams files to backup...");
                java.nio.file.Files.list(java.nio.file.Paths.get("."))
                        .filter(path -> {
                            String fileName = path.getFileName().toString();
                            // Check if filename starts with "formed_teams_" and ends with ".csv"
                            return fileName.startsWith("formed_teams_") && fileName.endsWith(".csv");
                        })
                        .forEach(teamFile -> {
                            try {
                                String teamFileName = teamFile.getFileName().toString();
                                String teamBackup = "backup_" + teamFileName;
                                java.nio.file.Files.copy(teamFile, java.nio.file.Paths.get(teamBackup),
                                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("✅ Teams file backed up to: " + teamBackup);
                            } catch (Exception e) {
                                System.out.println("❌ Failed to backup teams file: " + teamFile.getFileName());
                            }
                        });
            } catch (Exception e) {
                System.out.println("⚠️  No teams files found to backup");
            }

            System.out.println("✅ System backup completed successfully!");
            System.out.println("💡 All backups saved in root directory with timestamp.");

        } catch (Exception e) {
            System.out.println("❌ Backup failed: " + e.getMessage());
        }
    }

    // 🎯 PLAYER MENU (same for both registered and signed-in players)
    private static boolean showPlayerMenu() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("                    PLAYER DASHBOARD");
        System.out.println("═".repeat(60));
        System.out.println("Welcome, " + userManager.getCurrentUserName() + "!");
        System.out.println("1. View My Profile");
        System.out.println("2. View All Teams");
        System.out.println("3. Update My Survey");
        System.out.println("4. Logout");
        System.out.println("═".repeat(60));
        System.out.print("Choose an option (1-4): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    viewMyProfile();
                    break;
                case 2:
                    viewTeams();
                    break;
                case 3:
                    updateMySurvey();
                    break;
                case 4:
                    userManager.logout();
                    System.out.println("✅ Logged out successfully.");
                    break;
                default:
                    System.out.println("❌ Invalid choice. Please enter 1-4.");
            }
            return true;

        } catch (NumberFormatException e) {
            System.out.println("❌ Please enter a valid number.");
            return true;
        }
    }

    private static void importPlayersFromCSV() {
        try {
            System.out.print("Enter the path to players.csv file: ");
            String filePath = scanner.nextLine().trim();

            if (filePath.isEmpty()) {
                filePath = "players.csv"; // Default to root directory
            }

            System.out.println("📥 Importing players from: " + filePath);

            List<Participant> importedPlayers = fileHandler.loadPlayersFromCSV(filePath);
            System.out.println("✅ Found " + importedPlayers.size() + " players in the CSV file");

            // Filter out players that are already in the system
            List<Participant> newPlayers = fileHandler.getNewPlayers(importedPlayers, participants);

            if (newPlayers.isEmpty()) {
                System.out.println("💡 All players from this file are already in the system.");
                return;
            }

            // Add only new players to the system
            participants.addAll(newPlayers);
            System.out.println("✅ Added " + newPlayers.size() + " new players to the system");
            System.out.println("📊 Total participants in system: " + participants.size());

            // Show imported players
            System.out.println("\n📝 Newly Imported Players:");
            System.out.println("─".repeat(50));
            for (Participant player : newPlayers) {
                System.out.println("  • " + player.getName() + " - " + player.getEmail() + " - " + player.getGameInterest());
            }

        } catch (FileProcessingException e) {
            System.out.println("❌ Error importing players: " + e.getMessage());
            System.out.println("💡 Make sure the file exists and has the correct format.");
        }
    }

    private static void viewParticipants() {
        if (participants.isEmpty()) {
            System.out.println("❌ No participants loaded in the system.");
            System.out.println("💡 Please import players from a CSV file first using option 1.");
            return;
        }

        System.out.println("\n" + "─".repeat(80));
        System.out.println("               CURRENT PARTICIPANTS IN SYSTEM (" + participants.size() + ")");
        System.out.println("─".repeat(80));
        System.out.printf("%-12s %-20s %-25s %-15s %-4s %-12s %s%n",
                "ID", "Name", "Email", "Game", "Skill", "Role", "Personality");
        System.out.println("─".repeat(80));

        for (Participant p : participants) {
            System.out.printf("%-12s %-20s %-25s %-15s %-4d %-12s %s (%d)%n",
                    p.getParticipantId(),
                    p.getName(),
                    p.getEmail(),
                    p.getGameInterest(),
                    p.getSkillLevel(),
                    p.getPreferredRole(),
                    p.getPersonalityType(),
                    p.getPersonalityScore()
            );
        }
        System.out.println("─".repeat(80));
    }

    private static void formTeams() {
        if (participants.isEmpty()) {
            System.out.println("❌ No participants available. Please import participants first.");
            return;
        }

        System.out.print("Enter team size: ");
        try {
            int teamSize = Integer.parseInt(scanner.nextLine());

            List<String> errors = ValidationService.validateTeamSize(teamSize, participants.size());
            if (!errors.isEmpty()) {
                System.out.println("❌ Validation errors:");
                errors.forEach(System.out::println);
                return;
            }

            TeamBuilder teamBuilder = new TeamBuilder(teamSize, participants);
            teams = teamBuilder.formTeams();
            System.out.println("✅ Successfully formed " + teams.size() + " teams!");

        } catch (NumberFormatException e) {
            System.out.println("❌ Please enter a valid number for team size.");
        } catch (TeamFormationException e) {
            System.out.println("❌ Team formation failed: " + e.getMessage());
        }
    }

    private static void viewTeams() {
        if (teams.isEmpty()) {
            System.out.println("❌ No teams formed yet. Please form teams first.");
            return;
        }

        System.out.println("\n" + "═".repeat(80));
        System.out.println("                           FORMED TEAMS");
        System.out.println("═".repeat(80));

        for (Team team : teams) {
            System.out.println(team.toDetailedString());
            System.out.println("─".repeat(80));
        }

        System.out.println("Total teams: " + teams.size());
    }

    private static void saveTeamsToFormedFile() {
        if (teams.isEmpty()) {
            System.out.println("❌ No teams to save. Please form teams first.");
            return;
        }

        try {
            // Save to formed_teams directory with timestamp
            fileHandler.saveFormedTeams(teams);
            System.out.println("✅ Teams saved to formed_teams directory!");

        } catch (FileProcessingException e) {
            System.out.println("❌ Error saving teams: " + e.getMessage());
        }
    }

    private static void checkForNewSurveyPlayers() {
        try {
            // Check the default players.csv file
            String defaultPlayersFile = "players.csv";

            if (!fileHandler.isFileAccessible(defaultPlayersFile)) {
                System.out.println("❌ No players.csv file found in the root directory.");
                System.out.println("💡 Players need to complete surveys first.");
                return;
            }

            List<Participant> allSurveyPlayers = fileHandler.loadPlayersFromCSV(defaultPlayersFile);
            List<Participant> newPlayers = fileHandler.getNewPlayers(allSurveyPlayers, participants);

            if (newPlayers.isEmpty()) {
                System.out.println("✅ All survey players are already imported in the system.");
            } else {
                System.out.println("🎯 Found " + newPlayers.size() + " new players who completed surveys!");
                System.out.println("💡 Use 'Import Players from CSV File' to add them to the system.");

                System.out.println("\n📝 New Survey Players:");
                System.out.println("─".repeat(50));
                for (int i = 0; i < Math.min(5, newPlayers.size()); i++) {
                    Participant player = newPlayers.get(i);
                    System.out.println("  • " + player.getName() + " - " + player.getEmail() + " - " + player.getGameInterest());
                }
                if (newPlayers.size() > 5) {
                    System.out.println("  ... and " + (newPlayers.size() - 5) + " more");
                }
            }

        } catch (FileProcessingException e) {
            System.out.println("❌ Error checking for new players: " + e.getMessage());
        }
    }

    private static void showTeamAnalysis() {
        if (teams.isEmpty()) {
            System.out.println("❌ No teams formed yet. Please form teams first.");
            return;
        }

        try {
            System.out.println("🔍 Analyzing " + teams.size() + " teams...");

            TeamBuilder teamBuilder = new TeamBuilder(teams.get(0).getSize(), participants);
            Map<String, Object> analysis = teamBuilder.analyzeTeamFormation(teams);

            System.out.println("\n" + "📊 TEAM FORMATION ANALYSIS");
            System.out.println("─".repeat(50));

            if (analysis.isEmpty()) {
                System.out.println("❌ No analysis data available");
                return;
            }

            Set<Map.Entry<String, Object>> entries = analysis.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                String key = entry.getKey().replace("_", " ");
                Object value = entry.getValue();
                System.out.printf("%-30s: %s%n", key, value);
            }

            // Show additional team insights
            System.out.println("\n🎯 TEAM INSIGHTS");
            System.out.println("─".repeat(50));

            double totalSkill = teams.stream().mapToDouble(Team::getAverageSkill).sum();
            double avgSkill = totalSkill / teams.size();
            System.out.printf("%-30s: %.2f%n", "Overall average team skill", avgSkill);

            int teamsWithLeaders = (int) teams.stream().filter(Team::hasLeader).count();
            System.out.printf("%-30s: %d/%d (%.1f%%)%n", "Teams with natural leaders",
                    teamsWithLeaders, teams.size(), (teamsWithLeaders * 100.0) / teams.size());

            // Show team compositions
            System.out.println("\n👥 TEAM COMPOSITIONS");
            System.out.println("─".repeat(50));
            for (Team team : teams) {
                long leaders = team.getMembers().stream()
                        .filter(p -> p.getPersonalityType() == PersonalityType.LEADER).count();
                long thinkers = team.getMembers().stream()
                        .filter(p -> p.getPersonalityType() == PersonalityType.THINKER).count();
                long balanced = team.getMembers().stream()
                        .filter(p -> p.getPersonalityType() == PersonalityType.BALANCED).count();

                System.out.printf("%s: %d Leader, %d Thinker, %d Balanced (Avg Skill: %.1f)%n",
                        team.getTeamId(), leaders, thinkers, balanced, team.getAverageSkill());
            }

        } catch (Exception e) {
            System.out.println("❌ Error generating team analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewMyProfile() {
        String userEmail = userManager.getCurrentUserEmail();

        // First check in loaded participants (only if imported by organizer)
        Participant myProfile = participants.stream()
                .filter(p -> p.getEmail().equals(userEmail))
                .findFirst()
                .orElse(null);

        if (myProfile == null) {
            // If not in loaded participants, check survey players in file
            try {
                List<Participant> surveyPlayers = fileHandler.loadPlayersFromCSV("players.csv");
                myProfile = surveyPlayers.stream()
                        .filter(p -> p.getEmail().equals(userEmail))
                        .findFirst()
                        .orElse(null);
            } catch (FileProcessingException e) {
                // Ignore error, just show profile not found
            }
        }

        if (myProfile != null) {
            System.out.println("\n" + "👤 YOUR PROFILE");
            System.out.println("─".repeat(40));
            System.out.println("Name: " + myProfile.getName());
            System.out.println("Email: " + myProfile.getEmail());
            System.out.println("Participant ID: " + myProfile.getParticipantId());
            System.out.println("Game Interest: " + myProfile.getGameInterest());
            System.out.println("Skill Level: " + myProfile.getSkillLevel() + "/10");
            System.out.println("Preferred Role: " + myProfile.getPreferredRole());
            System.out.println("Personality: " + myProfile.getPersonalityType() + " (" + myProfile.getPersonalityScore() + "/100)");

            // Show which team you're in (only if organizer has formed teams)
            Team myTeam = teams.stream()
                    .filter(team -> team.getMembers().stream()
                            .anyMatch(p -> p.getEmail().equals(userEmail)))
                    .findFirst()
                    .orElse(null);

            if (myTeam != null) {
                System.out.println("\n🏆 YOUR TEAM: " + myTeam.getTeamName());
                System.out.println("Team Members:");
                myTeam.getMembers().forEach(member ->
                        System.out.println("  • " + member.getName() + " - " + member.getPreferredRole()));
            } else {
                System.out.println("\nℹ️  You are not currently assigned to a team.");
                System.out.println("💡 The organizer needs to import players and form teams.");
            }
        } else {
            System.out.println("❌ Profile not found. Please complete the survey first.");
            System.out.println("💡 Use 'Update My Survey' to create your profile.");
        }
    }

    private static void updateMySurvey() {
        String userEmail = userManager.getCurrentUserEmail();
        String userName = userManager.getCurrentUserName();

        System.out.println("\n🔄 UPDATE YOUR SURVEY");
        System.out.println("Note: This will update your existing gaming profile data.");

        // First, find the existing participant - ONLY in the file, not in memory
        Participant existingParticipant = null;

        try {
            List<Participant> surveyPlayers = fileHandler.loadPlayersFromCSV("players.csv");
            existingParticipant = surveyPlayers.stream()
                    .filter(p -> p.getEmail().equals(userEmail))
                    .findFirst()
                    .orElse(null);
        } catch (FileProcessingException e) {
            System.out.println("❌ Error loading existing survey data: " + e.getMessage());
            return; // Exit if we can't load the file
        }

        if (existingParticipant == null) {
            System.out.println("❌ No existing survey data found. Please complete the survey first.");
            return;
        }

        System.out.println("📝 Current Profile:");
        System.out.println("  • Participant ID: " + existingParticipant.getParticipantId());
        System.out.println("  • Name: " + existingParticipant.getName());
        System.out.println("  • Game: " + existingParticipant.getGameInterest());
        System.out.println("  • Skill Level: " + existingParticipant.getSkillLevel());
        System.out.println("  • Preferred Role: " + existingParticipant.getPreferredRole());
        System.out.println("  • Personality Score: " + existingParticipant.getPersonalityScore());

        System.out.println("\n🎯 Let's update your survey:");

        try {
            // Get updated information
            String currentGame = existingParticipant.getGameInterest();
            int currentSkill = existingParticipant.getSkillLevel();
            GameRole currentRole = existingParticipant.getPreferredRole();
            int currentPersonality = existingParticipant.getPersonalityScore();

            // Game interest
            String gameInterest = currentGame; // Default to current game
            System.out.println("Available Games:");
            for (String g : VALID_GAMES) {
                System.out.println(" - " + g);
            }
            System.out.println();

            while (true) {
                System.out.print("Enter your preferred game [" + currentGame + "] (press Enter to keep current): ");
                String gameInput = scanner.nextLine().trim();

                if (gameInput.isEmpty()) {
                    gameInterest = currentGame;
                    break;
                }

                if (ValidationService.isValidGame(gameInput)) {
                    // NORMALIZE the game name
                    gameInterest = ValidationService.normalizeGameName(gameInput);
                    break;
                } else {
                    System.out.println("❌ Invalid game. Please choose from the list above or press Enter to keep current.");
                }
            }

            // Skill level with validation
            int skillLevel = currentSkill;
            boolean validSkill = false;
            while (!validSkill) {
                try {
                    System.out.print("Enter your Skill Level (1-10) [" + currentSkill + "]: ");
                    String skillInput = scanner.nextLine().trim();
                    if (skillInput.isEmpty()) {
                        skillLevel = currentSkill;
                        validSkill = true;
                    } else {
                        skillLevel = Integer.parseInt(skillInput);
                        if (skillLevel >= 1 && skillLevel <= 10) {
                            validSkill = true;
                        } else {
                            System.out.println("❌ Please enter a number between 1 and 10.");
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ Please enter a valid number.");
                }
            }

            // Role selection
            GameRole role = currentRole;
            boolean validRole = false;
            while (!validRole) {
                System.out.println("\nAvailable Roles:");
                for (GameRole gameRole : GameRole.values()) {
                    System.out.println("  - " + gameRole.name() + ": " + gameRole.getDisplayName());
                }

                System.out.print("Enter your Preferred Role [" + currentRole + "]: ");
                String roleInput = scanner.nextLine().trim();
                if (roleInput.isEmpty()) {
                    role = currentRole;
                    validRole = true;
                } else {
                    try {
                        role = GameRole.valueOf(roleInput.toUpperCase());
                        validRole = true;
                    } catch (IllegalArgumentException e) {
                        System.out.println("❌ Invalid role. Please choose from: " +
                                String.join(", ", java.util.Arrays.stream(GameRole.values())
                                        .map(GameRole::name)
                                        .toArray(String[]::new)));
                    }
                }
            }

            // Personality assessment (optional - can skip)
            System.out.println("\n" + "=".repeat(50));
            System.out.println("      PERSONALITY ASSESSMENT (Optional)");
            System.out.println("=".repeat(50));
            System.out.println("Current Personality Score: " + currentPersonality + " (" +
                    existingParticipant.getPersonalityType() + ")");

            // Get valid retake choice (y/n)
            String retakeChoice;
            while (true) {
                System.out.print("Do you want to retake the personality assessment? (y/N): ");
                retakeChoice = scanner.nextLine().trim().toLowerCase();

                if (retakeChoice.isEmpty()) {
                    retakeChoice = "n"; // Default to "no" if empty
                    break;
                } else if (retakeChoice.equals("y") || retakeChoice.equals("yes") ||
                        retakeChoice.equals("n") || retakeChoice.equals("no")) {
                    break;
                } else {
                    System.out.println("❌ Please enter 'y' for yes or 'n' for no.");
                }
            }

            int personalityScore = currentPersonality;

            if (retakeChoice.equals("y") || retakeChoice.equals("yes")) {
                System.out.println("Please rate the following statements from 1 (Strongly Disagree) to 5 (Strongly Agree):");

                List<String> personalityQuestions = java.util.Arrays.asList(
                        "I prefer taking charge in group situations (1-5): ",
                        "I enjoy analyzing problems before taking action (1-5): ",
                        "I work well under pressure (1-5): ",
                        "I prefer following established procedures (1-5): ",
                        "I enjoy coming up with creative solutions (1-5): "
                );

                int totalScore = 0;
                for (int i = 0; i < personalityQuestions.size(); i++) {
                    int response = 0;
                    while (response < 1 || response > 5) {
                        try {
                            System.out.print((i + 1) + ". " + personalityQuestions.get(i));
                            String responseInput = scanner.nextLine().trim();
                            if (responseInput.isEmpty()) {
                                response = 3; // Default neutral response
                            } else {
                                response = Integer.parseInt(responseInput);
                                if (response < 1 || response > 5) {
                                    System.out.println("❌ Please enter a number between 1 and 5.");
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Please enter a valid number.");
                        }
                    }
                    totalScore += response;
                }

                // Convert to 50-100 scale
                personalityScore = 50 + (totalScore - 5) * 2;
                System.out.println("Your new personality score: " + personalityScore + "/100 (" +
                        com.teammate.models.PersonalityType.fromScore(personalityScore) + ")");
            }

            // Create updated participant with SAME ID
            Participant updatedParticipant = new Participant(
                    existingParticipant.getParticipantId(), // Keep the same ID
                    userName,
                    userEmail,
                    gameInterest,
                    skillLevel,
                    role,
                    personalityScore
            );

            // Validate the updated data
            List<String> errors = ValidationService.validateParticipantData(updatedParticipant);
            if (!errors.isEmpty()) {
                System.out.println("❌ Validation errors:");
                for (String error : errors) {
                    System.out.println("  - " + error);
                }
                return;
            }

            // ⚠️ CRITICAL: DO NOT update in-memory participants list
            // The participants list is ONLY for organizer-imported players

            // Update in file by rewriting the entire players.csv
            updatePlayerInFile(updatedParticipant);

            System.out.println("✅ Survey updated successfully!");
            System.out.println("📝 Your profile has been updated in the players.csv file.");
            System.out.println("💡 The organizer needs to import the CSV file to use your updated data in team formation.");

        } catch (Exception e) {
            System.out.println("❌ Error updating survey: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🎯 HELPER METHOD TO UPDATE PLAYER IN FILE
    private static void updatePlayerInFile(Participant updatedParticipant) {
        try {
            String playersFile = "players.csv";

            // Check if file exists
            if (!java.nio.file.Files.exists(java.nio.file.Paths.get(playersFile))) {
                System.out.println("❌ players.csv file not found. Creating new file.");
                fileHandler.savePlayerToSurveyFile(updatedParticipant);
                return;
            }

            // Read all lines from the file
            List<String> lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get(playersFile));
            List<String> updatedLines = new java.util.ArrayList<>();

            boolean headerProcessed = false;
            boolean participantUpdated = false;

            for (String line : lines) {
                // Keep header as-is
                if (!headerProcessed) {
                    updatedLines.add(line);
                    headerProcessed = true;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Check if this is the participant to update
                String[] fields = line.split(",");
                if (fields.length >= 1 && fields[0].trim().equals(updatedParticipant.getParticipantId())) {
                    // Replace with updated participant data
                    String updatedLine = String.format("%s,%s,%s,%s,%d,%s,%d",
                            updatedParticipant.getParticipantId(),
                            updatedParticipant.getName(),
                            updatedParticipant.getEmail(),
                            updatedParticipant.getGameInterest(),
                            updatedParticipant.getSkillLevel(),
                            updatedParticipant.getPreferredRole().name(),
                            updatedParticipant.getPersonalityScore()
                    );
                    updatedLines.add(updatedLine);
                    participantUpdated = true;
                } else {
                    // Keep other participants as-is
                    updatedLines.add(line);
                }
            }

            // If participant wasn't found in file, add them
            if (!participantUpdated) {
                String newLine = String.format("%s,%s,%s,%s,%d,%s,%d",
                        updatedParticipant.getParticipantId(),
                        updatedParticipant.getName(),
                        updatedParticipant.getEmail(),
                        updatedParticipant.getGameInterest(),
                        updatedParticipant.getSkillLevel(),
                        updatedParticipant.getPreferredRole().name(),
                        updatedParticipant.getPersonalityScore()
                );
                updatedLines.add(newLine);
            }

            // Write all lines back to file
            java.nio.file.Files.write(java.nio.file.Paths.get(playersFile), updatedLines);

            System.out.println("💾 Profile updated in players.csv file.");

        } catch (Exception e) {
            System.out.println("❌ Error updating file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}