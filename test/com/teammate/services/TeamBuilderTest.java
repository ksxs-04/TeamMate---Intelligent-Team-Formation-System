package com.teammate.services;

import com.teammate.models.*;
import com.teammate.exceptions.TeamFormationException;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class TeamBuilderTest {

    // TEST 1: Can form teams with valid data
    @Test
    public void canFormTeams() throws TeamFormationException {
        // Create 10 participants
        List<Participant> participants = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            participants.add(new Participant(
                    "User" + i,
                    "user" + i + "@iit.ac.lk",
                    "Valorant",
                    5 + i % 5,
                    GameRole.values()[i % GameRole.values().length],
                    60 + i * 3
            ));
        }

        TeamBuilder builder = new TeamBuilder(5, participants);
        List<Team> teams = builder.formTeams();

        assertEquals(2, teams.size()); // 10 participants / 5 team size = 2 teams
        assertEquals(5, teams.get(0).getSize());
        assertEquals(5, teams.get(1).getSize());
    }

    // TEST 2: Throws error when not enough participants
    @Test
    public void throwsErrorWhenInsufficientParticipants() {
        List<Participant> participants = new ArrayList<>();
        participants.add(new Participant("John", "john@iit.ac.lk", "Valorant", 7, GameRole.ATTACKER, 85));

        assertThrows(TeamFormationException.class, () -> {
            new TeamBuilder(5, participants).formTeams();
        });
    }
}