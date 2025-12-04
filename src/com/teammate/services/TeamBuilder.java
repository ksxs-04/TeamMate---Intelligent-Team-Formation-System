package com.teammate.services;

import com.teammate.models.Team;
import com.teammate.models.Participant;
import com.teammate.models.GameRole;
import com.teammate.models.PersonalityType;
import com.teammate.exceptions.TeamFormationException;

import java.util.*;

public class TeamBuilder {
    private final int teamSize;
    private final List<Participant> participants;

    public TeamBuilder(int teamSize, List<Participant> participants) throws TeamFormationException {
        List<String> errors = ValidationService.validateTeamSize(teamSize, participants.size());
        if (!errors.isEmpty()) {
            throw new TeamFormationException(String.join("; ", errors));
        }

        this.teamSize = teamSize;
        this.participants = new ArrayList<>(participants);
    }

    public List<Team> formTeams() throws TeamFormationException {
        return formTeamsWithFairDistribution();
    }

    public List<Team> formTeamsWithFairDistribution() throws TeamFormationException {
        List<Participant> availableParticipants = new ArrayList<>(participants);
        Collections.shuffle(availableParticipants);

        int totalTeams = availableParticipants.size() / teamSize;
        List<Team> teams = new ArrayList<>();

        System.out.println("🔧 Forming " + totalTeams + " teams with fair distribution:");
        System.out.println("   • At least 1 Leader (or highest score) per team");
        System.out.println("   • At least 1 Thinker per team (if available)");
        System.out.println("   • Balanced distribution of remaining spots");

        // Sort participants by personality type
        List<Participant> leaders = new ArrayList<>();
        List<Participant> thinkers = new ArrayList<>();
        List<Participant> balanced = new ArrayList<>();

        for (Participant p : availableParticipants) {
            switch (p.getPersonalityType()) {
                case LEADER: leaders.add(p); break;
                case THINKER: thinkers.add(p); break;
                case BALANCED: balanced.add(p); break;
            }
        }

        System.out.println("📊 Available personalities: " +
                leaders.size() + " Leaders, " +
                thinkers.size() + " Thinkers, " +
                balanced.size() + " Balanced");

        // Create empty teams first
        for (int i = 0; i < totalTeams; i++) {
            teams.add(new Team("T" + (i + 1), "Team " + (i + 1)));
        }

        // Phase 1: Distribute Leaders fairly (1 per team if possible)
        distributeLeadersFairly(teams, leaders);

        // Phase 2: Distribute Thinkers fairly (at least 1 per team if possible)
        distributeThinkersFairly(teams, thinkers);

        // Phase 3: Fill remaining spots with Balanced participants
        distributeBalancedFairly(teams, balanced);

        // Phase 4: Fill any remaining spots with leftover participants
        fillRemainingSpotsFairly(teams, leaders, thinkers, balanced);

        // Validate all teams are complete
        for (Team team : teams) {
            if (team.getSize() != teamSize) {
                throw new TeamFormationException(team.getTeamId() + " incomplete with only " + team.getSize() + " members");
            }
            printTeamComposition(team);
        }

        System.out.println("\n🎉 Successfully formed " + teams.size() + " teams with fair distribution!");
        return teams;
    }

    private void distributeLeadersFairly(List<Team> teams, List<Participant> leaders) {
//        System.out.println("\n👑 Distributing Leaders...");

        // Give each team one leader if possible
        for (Team team : teams) {
            if (!leaders.isEmpty() && team.getSize() == 0) {
                Participant leader = leaders.remove(0);
                team.addMember(leader);
//                System.out.println("   ✅ " + team.getTeamId() + " got Leader: " + leader.getName());
            }
        }

        // If we have extra leaders, distribute them to teams without leaders
        while (!leaders.isEmpty()) {
            Team teamWithoutLeader = teams.stream()
                    .filter(team -> !team.hasLeader() && team.getSize() < teamSize)
                    .findFirst()
                    .orElse(null);

            if (teamWithoutLeader != null) {
                Participant leader = leaders.remove(0);
                teamWithoutLeader.addMember(leader);
//                System.out.println("   ➕ " + teamWithoutLeader.getTeamId() + " got extra Leader: " + leader.getName());
            } else {
                break; // No teams need leaders
            }
        }
    }

    private void distributeThinkersFairly(List<Team> teams, List<Participant> thinkers) {
//        System.out.println("\n🤔 Distributing Thinkers...");

        // First, ensure each team gets at least one thinker if possible
        for (Team team : teams) {
            if (!thinkers.isEmpty() && countThinkers(team) == 0 && team.getSize() < teamSize) {
                Participant thinker = thinkers.remove(0);
                team.addMember(thinker);
//                System.out.println("   ✅ " + team.getTeamId() + " got Thinker: " + thinker.getName());
            }
        }

        // Distribute remaining thinkers evenly
        while (!thinkers.isEmpty()) {
            // Find team with fewest thinkers that can accept more members
            Team teamWithFewestThinkers = teams.stream()
                    .filter(team -> team.getSize() < teamSize)
                    .min(Comparator.comparingInt(this::countThinkers))
                    .orElse(null);

            if (teamWithFewestThinkers != null) {
                Participant thinker = thinkers.remove(0);
                teamWithFewestThinkers.addMember(thinker);
//                System.out.println("   ➕ " + teamWithFewestThinkers.getTeamId() + " got additional Thinker: " + thinker.getName());
            } else {
                break; // No teams can accept more members
            }
        }
    }

    private void distributeBalancedFairly(List<Team> teams, List<Participant> balanced) {
//        System.out.println("\n⚖️ Distributing Balanced participants...");

        // Distribute balanced participants evenly
        while (!balanced.isEmpty()) {
            // Find team with fewest members (to balance team sizes)
            Team teamWithFewestMembers = teams.stream()
                    .filter(team -> team.getSize() < teamSize)
                    .min(Comparator.comparingInt(Team::getSize))
                    .orElse(null);

            if (teamWithFewestMembers != null) {
                Participant balancedParticipant = balanced.remove(0);
                teamWithFewestMembers.addMember(balancedParticipant);
//                System.out.println("   ✅ " + teamWithFewestMembers.getTeamId() + " got Balanced: " + balancedParticipant.getName());
            } else {
                break; // All teams are full
            }
        }
    }

    private void fillRemainingSpotsFairly(List<Team> teams, List<Participant> leaders, List<Participant> thinkers, List<Participant> balanced) {
//        System.out.println("\n🔄 Filling remaining spots...");

        // Combine all leftover participants
        List<Participant> allLeftover = new ArrayList<>();
        allLeftover.addAll(leaders);
        allLeftover.addAll(thinkers);
        allLeftover.addAll(balanced);

        // Sort by personality score (highest first) for better team quality
        allLeftover.sort((p1, p2) -> Integer.compare(p2.getPersonalityScore(), p1.getPersonalityScore()));

        while (!allLeftover.isEmpty()) {
            // Find team with fewest members
            Team teamWithFewestMembers = teams.stream()
                    .filter(team -> team.getSize() < teamSize)
                    .min(Comparator.comparingInt(Team::getSize))
                    .orElse(null);

            if (teamWithFewestMembers != null) {
                Participant participant = allLeftover.remove(0);
                teamWithFewestMembers.addMember(participant);
//                System.out.println("   ➕ " + teamWithFewestMembers.getTeamId() + " got " +
//                        participant.getPersonalityType() + ": " + participant.getName());
            } else {
                break; // All teams are full
            }
        }
    }

    private int countThinkers(Team team) {
        return (int) team.getMembers().stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.THINKER)
                .count();
    }

    private void printTeamComposition(Team team) {
        long leaderCount = team.getMembers().stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.LEADER)
                .count();
        long thinkerCount = team.getMembers().stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.THINKER)
                .count();
        long balancedCount = team.getMembers().stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.BALANCED)
                .count();

        Participant teamLeader = team.getLeader();
        if (teamLeader == null) {
            teamLeader = team.getBackupLeader();
        }

        System.out.println("\n✅ " + team.getTeamId() + " Final Composition: " +
                leaderCount + " Leader, " +
                thinkerCount + " Thinker, " +
                balancedCount + " Balanced");

        if (teamLeader != null) {
            System.out.println("   🎯 Team Leader: " + teamLeader.getName() +
                    " (" + teamLeader.getPersonalityType() +
                    ", Score: " + teamLeader.getPersonalityScore() + ")");
        }

        // Show all team members
        System.out.println("   👥 Members:");
        for (Participant member : team.getMembers()) {
            System.out.println("      • " + member.getName() + " - " +
                    member.getPreferredRole() + " - " +
                    member.getPersonalityType() + " (" +
                    member.getPersonalityScore() + ")");
        }
    }

    // ... keep the same analyzeTeamFormation method from previous version
    public Map<String, Object> analyzeTeamFormation(List<Team> teams) {
        Map<String, Object> analysis = new HashMap<>();

        if (teams == null || teams.isEmpty()) {
            analysis.put("error", "No teams to analyze");
            return analysis;
        }

        // Basic team statistics
        analysis.put("total_teams", teams.size());
        analysis.put("total_participants", teams.stream().mapToInt(Team::getSize).sum());

        // Personality composition analysis
        int teamsWithIdealComposition = 0;
        int teamsWithLeader = 0;
        int teamsWithOptimalThinkers = 0;
        int teamsWithBackupLeader = 0;

        for (Team team : teams) {
            long leaderCount = team.getMembers().stream()
                    .filter(p -> p.getPersonalityType() == PersonalityType.LEADER)
                    .count();
            long thinkerCount = team.getMembers().stream()
                    .filter(p -> p.getPersonalityType() == PersonalityType.THINKER)
                    .count();

            // Check if team has a leader (actual Leader or backup)
            boolean hasLeader = team.getLeader() != null || team.getBackupLeader() != null;
            boolean hasOptimalThinkers = thinkerCount >= 1 && thinkerCount <= 2;

            if (hasLeader && hasOptimalThinkers) {
                teamsWithIdealComposition++;
            }
            if (hasLeader) teamsWithLeader++;
            if (hasOptimalThinkers) teamsWithOptimalThinkers++;

            // Count teams with backup leaders
            if (team.getLeader() == null && team.getBackupLeader() != null) {
                teamsWithBackupLeader++;
            }
        }

        analysis.put("teams_with_ideal_composition", teamsWithIdealComposition);
        analysis.put("ideal_composition_percentage",
                String.format("%.1f%%", (teamsWithIdealComposition * 100.0) / teams.size()));
        analysis.put("teams_with_leader", teamsWithLeader);
        analysis.put("teams_with_optimal_thinkers", teamsWithOptimalThinkers);
        analysis.put("teams_with_backup_leader", teamsWithBackupLeader);

        // Skill level analysis
        double avgTeamSkill = teams.stream()
                .mapToDouble(Team::getAverageSkill)
                .average()
                .orElse(0.0);
        analysis.put("average_team_skill", String.format("%.2f", avgTeamSkill));

        // Role diversity analysis
        int teamsWithRoleDiversity = 0;
        for (Team team : teams) {
            long uniqueRoles = team.getMembers().stream()
                    .map(Participant::getPreferredRole)
                    .distinct()
                    .count();
            if (uniqueRoles >= 3) {
                teamsWithRoleDiversity++;
            }
        }
        analysis.put("teams_with_role_diversity", teamsWithRoleDiversity);

        // Game diversity analysis
        int teamsWithGameDiversity = 0;
        for (Team team : teams) {
            if (team.getGameInterests().size() >= 2) {
                teamsWithGameDiversity++;
            }
        }
        analysis.put("teams_with_game_diversity", teamsWithGameDiversity);

        return analysis;
    }
}