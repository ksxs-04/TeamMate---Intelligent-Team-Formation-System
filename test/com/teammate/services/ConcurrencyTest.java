package com.teammate.services;

import com.teammate.models.Participant;
import com.teammate.models.GameRole;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

public class ConcurrencyTest {

    @Test
    public void testThreadSafeIdGeneration() throws InterruptedException {
        Set<String> ids = Collections.synchronizedSet(new HashSet<>());
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                // Each thread creates a participant
                Participant p = new Participant("Test", "test@iit.ac.lk",
                        "Valorant", 5, GameRole.ATTACKER, 75);
                ids.add(p.getParticipantId());
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(threadCount, ids.size(),
                "All " + threadCount + " IDs should be unique");
    }
}