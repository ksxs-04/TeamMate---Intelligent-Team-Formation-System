package com.teammate.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Team {
    private String teamId;
    private String teamName;
    private List<Participant> members;
    private double averageSkill;

    public Team(String teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.members = new ArrayList<>();
        this.averageSkill = 0.0;
    }

    // Add a member to the team
    public void addMember(Participant participant) {
        members.add(participant);
        updateAverageSkill();
    }

    // Remove a member from the team
    public void removeMember(Participant participant) {
        members.remove(participant);
        updateAverageSkill();
    }

    // Update average skill when members change
    private void updateAverageSkill() {
        if (!members.isEmpty()) {
            this.averageSkill = members.stream()
                    .mapToInt(Participant::getSkillLevel)
                    .average()
                    .orElse(0.0);
        } else {
            this.averageSkill = 0.0;
        }
    }


    // Get all unique game interests in the team
    public List<String> getGameInterests() {
        return members.stream()
                .map(Participant::getGameInterest)
                .distinct()
                .collect(Collectors.toList());
    }

    // Check if team has a leader (PersonalityType.LEADER)
    public boolean hasLeader() {
        return members.stream().anyMatch(p -> p.getPersonalityType() == PersonalityType.LEADER);
    }

    // Get the team leader (or null if no leader)
    public Participant getLeader() {
        return members.stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.LEADER)
                .findFirst()
                .orElse(null);
    }

    // Get backup leader (participant with highest personality score)
    public Participant getBackupLeader() {
        return members.stream()
                .max((p1, p2) -> Integer.compare(p1.getPersonalityScore(), p2.getPersonalityScore()))
                .orElse(null);
    }

    // Getters
    public String getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public List<Participant> getMembers() { return new ArrayList<>(members); } // Return copy
    public double getAverageSkill() { return averageSkill; }
    public int getSize() { return members.size(); }

    @Override
    public String toString() {
        return String.format("%s - %s (Size: %d, Avg Skill: %.1f)",
                teamId, teamName, getSize(), averageSkill);
    }

    // Detailed string representation
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(toString()).append("\n");
        sb.append("Members:\n");

        for (Participant member : members) {
            sb.append("  • ").append(member.toString()).append("\n");
        }

        sb.append("Game Interests: ").append(String.join(", ", getGameInterests())).append("\n");

        Participant leader = getLeader();
        if (leader != null) {
            sb.append("Team Leader: ").append(leader.getName()).append("\n");
        } else {
            Participant backup = getBackupLeader();
            sb.append("Backup Leader: ").append(backup.getName())
                    .append(" (Score: ").append(backup.getPersonalityScore()).append(")\n");
        }

        return sb.toString();
    }
}