package org.example.pvp.services;

import org.example.pvp.interfaces.RankingService;
import org.example.pvp.model.Player;
import org.example.pvp.model.Rank;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VersusRankingServiceTest {

    private final RankingService service = new VersusRankingService(createRankLowerBoundaries());

    @Test
    void shouldAddPlayersToRank() {
        Player player = new Player(1, 1000);
        List<Player> players = new ArrayList<>();
        players.add(player);

        service.addPlayers(players);

        List<Player> ranking = service.getRanking(Rank.BRONZE_3);
        assertEquals(1, ranking.size());
        assertEquals(player.getId(), ranking.getFirst().getId());
    }

    @Test
    void shouldAdd2PlayersAndRankThem() {
        Player player1 = new Player(1, 1000);
        Player player2 = new Player(2, 1500);

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        service.addPlayers(players);

        List<Player> bronzeRanking = service.getRanking(Rank.BRONZE_3);
        List<Player> silverRanking = service.getRanking(Rank.SILVER_2);

        assertEquals(1, bronzeRanking.size());
        assertEquals(player1.getId(), bronzeRanking.getFirst().getId());

        assertEquals(1, silverRanking.size());
        assertEquals(player2.getId(), silverRanking.getFirst().getId());
    }

    @Test
    void shouldAdd3PlayersAndRankThem() {
        Player player1 = new Player(1, 1000);
        Player player2 = new Player(2, 1500);
        Player player3 = new Player(3, 2000);

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        players.add(player3);

        service.addPlayers(players);

        List<Player> bronzeRanking = service.getRanking(Rank.BRONZE_3);
        List<Player> silverRanking = service.getRanking(Rank.SILVER_2);
        List<Player> goldRanking = service.getRanking(Rank.GOLD_2);

        assertEquals(1, bronzeRanking.size());
        assertEquals(player1.getId(), bronzeRanking.getFirst().getId());

        assertEquals(1, silverRanking.size());
        assertEquals(player2.getId(), silverRanking.getFirst().getId());

        assertEquals(1, goldRanking.size());
        assertEquals(player3.getId(), goldRanking.getFirst().getId());
    }

    @Test
    void shouldRemovePlayerFromRank() {
        Player player = new Player(1, 1000);

        List<Player> players = List.of(player);

        service.addPlayers(players);
        service.removePlayers(players);

        List<Player> ranking = service.getRanking(Rank.BRONZE_3);
        assertEquals(0, ranking.size());
    }

    @Test
    void shouldRemovePlayerFromRankAndRankThem() {
        Player player1 = new Player(1, 1000);
        Player player2 = new Player(2, 1100);

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        service.addPlayers(players);

        List<Player> ranking = service.getRanking(Rank.BRONZE_3);

        assertEquals(2, ranking.size());
        assertEquals(player2.getId(), ranking.getFirst().getId());

        service.removePlayers(List.of(player2));

        ranking = service.getRanking(Rank.BRONZE_3);
        assertEquals(1, ranking.size());
        assertEquals(player1.getId(), ranking.getFirst().getId());
    }

    @Test
    void shouldUpdate1PlayerRanking() {
        Player player = new Player(1, 1000);
        List<Player> players = List.of(player);

        service.addPlayers(players);

        player.setRating(2000); // Update the rating
        service.updateRankings(players);

        List<Player> ranking = service.getRanking(Rank.GOLD_2);
        assertEquals(1, ranking.size());
        assertEquals(player.getId(), ranking.getFirst().getId());
    }

    @Test
    void shouldUpdate2PlayersRanking() {
        Player player1 = new Player(1, 1000);
        Player player2 = new Player(2, 1100);

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        service.addPlayers(players);

        player1.setRating(3800); // Update the rating
        player2.setRating(3700); // Update the rating

        service.updateRankings(players);

        List<Player> ranking = service.getRanking(Rank.DIAMOND_2);

        assertEquals(2, ranking.size());
        assertEquals(player1.getId(), ranking.getFirst().getId());
        assertEquals(player2.getId(), ranking.getLast().getId());
    }

    @Test
    void shouldGetAllRanking() {
        Player player1 = new Player(1, 1000);
        Player player2 = new Player(2, 1100);
        Player player3 = new Player(3, 1200);

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        players.add(player3);

        service.addPlayers(players);

        List<Player> ranking = service.getRanking();

        assertEquals(3, ranking.size());
        assertEquals(player3.getId(), ranking.get(0).getId());
        assertEquals(player2.getId(), ranking.get(1).getId());
        assertEquals(player1.getId(), ranking.get(2).getId());
    }

    private static Map<Integer, Rank> createRankLowerBoundaries() {
        Map<Integer, Rank> rankBoundaries = new HashMap<>();

        rankBoundaries.put(0, Rank.BRONZE_1);
        rankBoundaries.put(500, Rank.BRONZE_2);
        rankBoundaries.put(1000, Rank.BRONZE_3);
        rankBoundaries.put(1200, Rank.SILVER_1);
        rankBoundaries.put(1400, Rank.SILVER_2);
        rankBoundaries.put(1600, Rank.SILVER_3);
        rankBoundaries.put(1800, Rank.GOLD_1);
        rankBoundaries.put(2000, Rank.GOLD_2);
        rankBoundaries.put(2200, Rank.GOLD_3);
        rankBoundaries.put(2400, Rank.PLATINUM_1);
        rankBoundaries.put(2600, Rank.PLATINUM_2);
        rankBoundaries.put(2800, Rank.PLATINUM_3);
        rankBoundaries.put(3200, Rank.DIAMOND_1);
        rankBoundaries.put(3600, Rank.DIAMOND_2);
        rankBoundaries.put(4000, Rank.DIAMOND_3);
        rankBoundaries.put(4400, Rank.MASTER);
        rankBoundaries.put(4800, Rank.GRANDMASTER);

        return rankBoundaries;
    }
}