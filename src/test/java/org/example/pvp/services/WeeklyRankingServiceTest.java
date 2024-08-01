package org.example.pvp.services;

import org.example.pvp.model.Player;
import org.example.pvp.model.Rank;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("Disabled until the WeeklyRankingService is properly implemented")
public class WeeklyRankingServiceTest {

    private final WeeklyRankingService rankingService = new WeeklyRankingService();

    @Test
    public void testMoveUpAndDown() {
        List<Player> players = new ArrayList<>();
        players.add(new Player(0, Rank.BRONZE_2, 1200));
        players.add(new Player(1, Rank.BRONZE_2, 1100));
        players.add(new Player(2, Rank.BRONZE_2, 1000));

        players.add(new Player(3, Rank.BRONZE_1, 820));
        players.add(new Player(4, Rank.BRONZE_1, 810));
        players.add(new Player(5, Rank.BRONZE_1, 800));

        rankingService.addPlayers(players);
        rankingService.rankPlayers();

        List<Player> bronze1Ranking = rankingService.getRanking(Rank.BRONZE_1);
        assertEquals(1, bronze1Ranking.get(0).getId());
        assertEquals(2, bronze1Ranking.get(1).getId());

        List<Player> bronze2Ranking = rankingService.getRanking(Rank.BRONZE_2);
        assertEquals(3, bronze2Ranking.get(1).getId());
        assertEquals(4, bronze2Ranking.get(2).getId());
    }

    @Test
    public void testMoveOnlyUpWhenUpperDoesntHaveEnoughPlayersToMoveDown() {
        List<Player> players = new ArrayList<>();
        players.add(new Player(0, Rank.BRONZE_1, 1200));
        players.add(new Player(1, Rank.BRONZE_1, 1100));
        players.add(new Player(2, Rank.BRONZE_1, 1000));

        rankingService.addPlayers(players);
        rankingService.rankPlayers();

        List<Player> bronze1Ranking = rankingService.getRanking(Rank.BRONZE_1);
        assertEquals(1, bronze1Ranking.size());
        assertEquals(2, bronze1Ranking.get(0).getId());

        List<Player> bronze2Ranking = rankingService.getRanking(Rank.BRONZE_2);
        assertEquals(2, bronze2Ranking.size());
        assertEquals(0, bronze2Ranking.get(0).getId());
        assertEquals(1, bronze2Ranking.get(1).getId());
    }

    @Test
    public void testMoveOnlyDownWhenLowerDoesntHaveEnoughPlayersToMoveUp() {
        List<Player> players = new ArrayList<>();
        players.add(new Player(0, Rank.GOLD_1, 1200));
        players.add(new Player(1, Rank.GOLD_1, 1180));
        players.add(new Player(2, Rank.GOLD_1, 1170));

        rankingService.addPlayers(players);
        rankingService.rankPlayers();

        List<Player> gold1Ranking = rankingService.getRanking(Rank.GOLD_1);
        assertEquals(1, gold1Ranking.size());
        assertEquals(0, gold1Ranking.get(0).getId());

        List<Player> silver3Ranking = rankingService.getRanking(Rank.SILVER_3);
        assertEquals(2, silver3Ranking.size());
        assertEquals(1, silver3Ranking.get(0).getId());
        assertEquals(2, silver3Ranking.get(1).getId());
    }
}
