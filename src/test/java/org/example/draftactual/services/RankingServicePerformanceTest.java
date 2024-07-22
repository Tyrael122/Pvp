package org.example.draftactual.services;

import org.example.draftactual.interfaces.RankingService;
import org.example.draftactual.model.Player;
import org.example.draftactual.model.Rank;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class RankingServicePerformanceTest {

    private final RankingService versusTreeRankingService = new VersusRankingService();

    @Test
    public void testTimeFor10000Players() {
        List<Player> players = createPlayers(10000);
        versusTreeRankingService.addPlayers(players);

        Player player = players.get(5).withRating(1500);

        var pl = PerformanceLogger.start();
        versusTreeRankingService.updateRankings(List.of(player));
        pl.stop();
    }

    private List<Player> createPlayers(int number) {
        List<Player> players = new ArrayList<>(number);

        for (int i = 0; i < number; i++) {
            players.add(new Player(i, Rank.UNRANKED, 200 + 10 * i));
        }

        return players;
    }

    private static class PerformanceLogger {
        private final long startTime;

        public PerformanceLogger() {
            startTime = System.nanoTime();
        }

        public static PerformanceLogger start() {
            return new PerformanceLogger();
        }

        public void stop() {
            long endTime = System.nanoTime();
            System.out.println("Elapsed time: " + (endTime - startTime));
        }
    }
}