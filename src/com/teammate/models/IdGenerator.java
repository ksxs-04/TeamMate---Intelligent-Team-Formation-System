package com.teammate.models;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class IdGenerator {
    private static final AtomicInteger participantCounter = new AtomicInteger(1);
    private static final AtomicInteger teamCounter = new AtomicInteger(1);
    private static final Pattern PARTICIPANT_ID_PATTERN = Pattern.compile("P\\d+");

    public static String generateParticipantId() {
        // Get the current value, then increment for next time
        int nextId = participantCounter.getAndIncrement();
        return "P" + String.format("%04d", nextId);
    }

    public static void initializeCounter(List<Participant> participants) {
        try {
            int maxId = 0;

            // Check existing participants in memory
            for (Participant p : participants) {
                String id = p.getParticipantId();
                if (id != null && id.startsWith("P")) {
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

            // Also check players.csv file
            File playersFile = new File("players.csv");
            if (playersFile.exists()) {
                List<String> lines = Files.readAllLines(playersFile.toPath());
                for (int i = 1; i < lines.size(); i++) { // Skip header
                    String line = lines.get(i);
                    if (!line.trim().isEmpty()) {
                        String[] parts = line.split(",");
                        if (parts.length > 0 && parts[0].startsWith("P")) {
                            try {
                                int idNum = Integer.parseInt(parts[0].substring(1));
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

            // Update the AtomicInteger counter
            if (maxId > 0) {
                // Set to maxId + 1 because next ID should be one more than max
                participantCounter.set(maxId + 1);
            }

            System.out.println("Next ID will be: P" +
                    String.format("%04d", participantCounter.get()));

        } catch (Exception e) {
            System.err.println("Error initializing ID counter: " + e.getMessage());
            participantCounter.set(1); // Reset to default
        }
    }

    public static void updateCounter(String participantId) {
        if (participantId != null && participantId.startsWith("P")) {
            try {
                int idNum = Integer.parseInt(participantId.substring(1));
                int currentCount = participantCounter.get();
                // Update if this ID is greater than what we think is next
                if (idNum >= currentCount) {
                    participantCounter.set(idNum + 1);
                    System.out.println("Next ID will be: P" +
                            String.format("%04d", participantCounter.get()));
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid participant ID format: " + participantId);
            }
        }
    }

    public static int getCurrentParticipantCount() {
        // Returns the last generated ID number (not the next one)
        return participantCounter.get() - 1;
    }

    public static void resetCounters() {
        participantCounter.set(1);
        teamCounter.set(1);
    }

    // Helper method to get the next ID without generating it
    public static String getNextParticipantId() {
        return "P" + String.format("%04d", participantCounter.get());
    }
}