package com.teammate.auth;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CredentialManager {
    private static final String PLAYERS_FILE = "player_credentials.csv";
    private Map<String, String> playerPasswords; // username -> password
    private Map<String, String> playerEmails;    // username -> email
    private Map<String, String> playerNames;     // username -> display name

    public CredentialManager() {
        playerPasswords = new HashMap<>();
        playerEmails = new HashMap<>();
        playerNames = new HashMap<>();
        loadPlayers();
    }

    // 🎯 SIMPLE FILE LOADING FOR PLAYERS
    private void loadPlayers() {
        try {
            if (!Files.exists(Paths.get(PLAYERS_FILE))) {
                System.out.println("📝 No existing player data found. Starting fresh.");
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(PLAYERS_FILE));
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String email = parts[2].trim();
                    String name = parts[3].trim();

                    playerPasswords.put(username, password);
                    playerEmails.put(username, email);
                    playerNames.put(username, name);
                }
            }
            reader.close();

        } catch (IOException e) {
            System.out.println("❌ Error loading players: " + e.getMessage());
        }
    }

    // 🎯 SAVE PLAYER CREDENTIALS TO CSV
    private void savePlayers() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(PLAYERS_FILE));

            // Write header
            writer.println("Username,Password,Email,Name");

            // Write player data
            for (String username : playerPasswords.keySet()) {
                String line = username + "," +
                        playerPasswords.get(username) + "," +
                        playerEmails.get(username) + "," +
                        playerNames.get(username);
                writer.println(line);
            }
            writer.close();

            System.out.println("✅ Saved " + playerPasswords.size() + " players to file.");

        } catch (IOException e) {
            System.out.println("❌ Error saving players: " + e.getMessage());
        }
    }

    // 🎯 ORGANIZER LOGIN (HARDCODED - SIMPLE!)
    public boolean authenticateOrganizer(String username, String password) {
        return "admin".equals(username) && "admin123".equals(password);
    }

    // 🎯 PLAYER REGISTRATION
    public boolean registerPlayer(String username, String password, String email, String name) {
        if (playerPasswords.containsKey(username)) {
            System.out.println("❌ Username already exists!");
            return false;
        }

        // Check if email already registered
        if (playerEmails.containsValue(email)) {
            System.out.println("❌ Email already registered!");
            return false;
        }

        playerPasswords.put(username, password);
        playerEmails.put(username, email);
        playerNames.put(username, name);

        savePlayers();
        System.out.println("✅ Player registered successfully: " + username);
        return true;
    }

    // 🎯 PLAYER LOGIN
    public boolean authenticatePlayer(String username, String password) {
        if (!playerPasswords.containsKey(username)) {
            return false; // Username doesn't exist
        }

        String storedPassword = playerPasswords.get(username);
        return storedPassword.equals(password);
    }

    // 🎯 GET PLAYER INFO
    public String getPlayerEmail(String username) {
        return playerEmails.get(username);
    }

    public String getPlayerName(String username) {
        return playerNames.get(username);
    }

    public String getPlayerUsernameByEmail(String email) {
        for (Map.Entry<String, String> entry : playerEmails.entrySet()) {
            if (entry.getValue().equals(email)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // 🎯 CHECK IF USERNAME EXISTS
    public boolean usernameExists(String username) {
        return playerPasswords.containsKey(username);
    }

    // 🎯 CHECK IF EMAIL EXISTS
    public boolean emailExists(String email) {
        return playerEmails.containsValue(email);
    }

    // 🎯 GET PLAYER COUNT
    public int getPlayerCount() {
        return playerPasswords.size();
    }
}