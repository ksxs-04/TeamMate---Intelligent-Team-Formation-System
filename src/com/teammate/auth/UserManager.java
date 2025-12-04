package com.teammate.auth;

import com.teammate.services.ValidationService;

import java.util.Scanner;
import java.util.List;

public class UserManager {
    private CredentialManager credentialManager;
    private String currentUser;
    private String userRole;
    private String currentUsername; // For players

    public UserManager() {
        this.credentialManager = new CredentialManager();
    }

    public boolean organizerLogin(Scanner scanner) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        ORGANIZER LOGIN");
        System.out.println("=".repeat(40));

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (credentialManager.authenticateOrganizer(username, password)) {
            currentUser = username;
            userRole = "organizer";
            System.out.println("🎉 Welcome, Organizer " + username + "!");
            return true;
        } else {
            System.out.println("❌ Invalid credentials. Please try again.");
            return false;
        }
    }

    // 🎯 NEW: PLAYER ACCESS MENU
    public boolean playerAccess(Scanner scanner) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("         PLAYER ACCESS");
        System.out.println("=".repeat(40));
        System.out.println("1. Register");
        System.out.println("2. Sign In");
        System.out.println("3. Back to Main Menu");
        System.out.println("=".repeat(40));
        System.out.print("Choose an option (1-3): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    return playerRegister(scanner);
                case 2:
                    return playerSignIn(scanner);
                case 3:
                    return false;
                default:
                    System.out.println("❌ Invalid choice. Please enter 1, 2, or 3.");
                    return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Please enter a valid number.");
            return false;
        }
    }

    // 🎯 PLAYER REGISTRATION
    private boolean playerRegister(Scanner scanner) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        PLAYER REGISTRATION");
        System.out.println("=".repeat(40));

        // Get email
        String email = "";
        while (email.isEmpty()) {
            System.out.print("Enter your IIT email: ");
            email = scanner.nextLine().trim();

            if (!ValidationService.isValidEmail(email)) {
                System.out.println("❌ Invalid email. Must be a valid @iit.ac.lk address.");
                email = "";
            } else if (credentialManager.emailExists(email)) {
                System.out.println("❌ Email already registered. Use sign in instead.");
                email = "";
            }
        }

        // Get name
        String name = "";
        List<String> nameErrors;
        do {
            System.out.print("Enter your full name: ");
            name = scanner.nextLine().trim();

            nameErrors = ValidationService.validateName(name);
            if (!nameErrors.isEmpty()) {
                System.out.println("❌ " + nameErrors.get(0));
            }
        } while (!nameErrors.isEmpty());

        // Get username
        String username = "";
        while (username.isEmpty()) {
            System.out.print("Choose a username: ");
            username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("❌ Username cannot be empty.");
            } else if (credentialManager.usernameExists(username)) {
                System.out.println("❌ Username already taken. Choose another.");
                username = "";
            }
        }

        // Get password
        String password = "";
        while (password.isEmpty()) {
            System.out.print("Choose a password: ");
            password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("❌ Password cannot be empty.");
            } else if (password.length() < 3) {
                System.out.println("❌ Password must be at least 3 characters.");
                password = "";
            }
        }



        // Register player
        if (credentialManager.registerPlayer(username, password, email, name)) {
            currentUsername = username;
            currentUser = email;
            userRole = "player";
            System.out.println("🎉 Registration successful! Welcome, " + name + "!");
            return true;
        }

        return false;
    }

    // 🎯 PLAYER SIGN IN
    private boolean playerSignIn(Scanner scanner) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("          PLAYER SIGN IN");
        System.out.println("=".repeat(40));

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (credentialManager.authenticatePlayer(username, password)) {
            currentUsername = username;
            currentUser = credentialManager.getPlayerEmail(username);
            userRole = "player";
            String playerName = credentialManager.getPlayerName(username);
            System.out.println("🎉 Welcome back, " + playerName + "!");
            return true;
        } else {
            System.out.println("❌ Invalid username or password.");
            return false;
        }
    }

    public void logout() {
        currentUser = null;
        userRole = null;
        currentUsername = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isOrganizer() {
        return "organizer".equals(userRole);
    }

    public boolean isPlayer() {
        return "player".equals(userRole);
    }

    public String getCurrentUserName() {
        if (currentUser == null) return "Guest";

        if (isOrganizer()) {
            return currentUser; // Organizer username
        } else {
            return credentialManager.getPlayerName(currentUsername);
        }
    }

    public String getCurrentUserEmail() {
        return currentUser;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }
}