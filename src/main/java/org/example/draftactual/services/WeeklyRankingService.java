package org.example.draftactual.services;

import org.example.draftactual.model.Player;
import org.example.draftactual.model.Rank;

import java.util.*;

public class WeeklyRankingService {
    private final Map<Rank, List<Player>> ranking = new HashMap<>();
    private final Map<Rank, Integer> moveUpCounts = createMoveUpCounts();
    private final Map<Rank, Integer> moveDownCounts = createMoveDownCounts();

    public void addPlayers(List<Player> players) {
        for (Player player : players) {
            if (player.getRank() == null) {
                continue;
            }

            if (!ranking.containsKey(player.getRank())) {
                ranking.put(player.getRank(), new ArrayList<>());
            }

            ranking.get(player.getRank()).add(player);
        }
    }

    public void removePlayers(List<Player> players) {
        for (Player player : players) {
            if (player.getRank() == null) {
                continue;
            }

            if (!ranking.containsKey(player.getRank())) {
                continue;
            }

            ranking.get(player.getRank()).remove(player);
        }
    }

    public void rankPlayers() {
        for (Map.Entry<Rank, List<Player>> entry : ranking.entrySet()) {
            entry.getValue().sort((p1, p2) -> (int) (p2.getRating() - p1.getRating()));
        }

        Rank[] ranks = Rank.values();
        for (int i = 0; i < ranks.length - 1; i++) {
            if (ranks[i] == Rank.UNRANKED) {
                continue;
            }

            movePlayers(ranks[i], ranks[i + 1]);
        }
    }

    public List<Player> getRanking() {
        List<Player> result = new ArrayList<>();

        for (Rank rank : Rank.values()) {
            result.addAll(ranking.getOrDefault(rank, List.of()));
        }

        return result;
    }

    public List<Player> getRanking(Rank rank) {
        return ranking.getOrDefault(rank, List.of());
    }

    private void movePlayers(Rank lowerRank, Rank upperRank) {
        List<Player> upperRankPlayers = ranking.getOrDefault(upperRank, List.of());
        List<Player> lowerRankPlayers = ranking.getOrDefault(lowerRank, List.of());

        int moveUpCount = moveUpCounts.get(upperRank);
        int moveDownCount = moveDownCounts.get(lowerRank);

        if (lowerRankPlayers.size() <= moveUpCount) {
            moveUpCount = 0;
        }

        if (upperRankPlayers.size() <= moveDownCount) {
            moveDownCount = 0;
        }

        List<Player> playersGoingUp = lowerRankPlayers.subList(0, moveUpCount);
        List<Player> playersGoingDown = upperRankPlayers.subList(upperRankPlayers.size() - moveDownCount, upperRankPlayers.size());

        upperRankPlayers = new ArrayList<>(upperRankPlayers.subList(0, upperRankPlayers.size() - moveDownCount));
        lowerRankPlayers = new ArrayList<>(lowerRankPlayers.subList(moveUpCount, lowerRankPlayers.size()));

        upperRankPlayers.addAll(playersGoingUp);
        lowerRankPlayers.addAll(0, playersGoingDown);

        changePlayerRanks(playersGoingUp, upperRank);
        changePlayerRanks(playersGoingDown, lowerRank);

        ranking.put(upperRank, upperRankPlayers);
        ranking.put(lowerRank, lowerRankPlayers);
    }

    private void changePlayerRanks(List<Player> players, Rank rank) {
        for (Player player : players) {
//            player.setRank(rank);
        }
    }

    private Map<Rank, Integer> createMoveUpCounts() {
        Map<Rank, Integer> moveUpCounts = new HashMap<>();

        moveUpCounts.put(Rank.BRONZE_1, 2);
        moveUpCounts.put(Rank.BRONZE_2, 2);
        moveUpCounts.put(Rank.BRONZE_3, 2);
        moveUpCounts.put(Rank.SILVER_1, 2);
        moveUpCounts.put(Rank.SILVER_2, 2);
        moveUpCounts.put(Rank.SILVER_3, 2);
        moveUpCounts.put(Rank.GOLD_1, 2);
        moveUpCounts.put(Rank.GOLD_2, 2);
        moveUpCounts.put(Rank.GOLD_3, 2);
        moveUpCounts.put(Rank.PLATINUM_1, 2);
        moveUpCounts.put(Rank.PLATINUM_2, 2);
        moveUpCounts.put(Rank.PLATINUM_3, 2);
        moveUpCounts.put(Rank.DIAMOND_1, 2);
        moveUpCounts.put(Rank.DIAMOND_2, 2);
        moveUpCounts.put(Rank.DIAMOND_3, 2);
        moveUpCounts.put(Rank.MASTER, 2);
        moveUpCounts.put(Rank.GRANDMASTER, 2);

        return moveUpCounts;
    }

    private Map<Rank, Integer> createMoveDownCounts() {
        Map<Rank, Integer> moveDownCounts = new HashMap<>();

        moveDownCounts.put(Rank.BRONZE_1, 2);
        moveDownCounts.put(Rank.BRONZE_2, 2);
        moveDownCounts.put(Rank.BRONZE_3, 2);
        moveDownCounts.put(Rank.SILVER_1, 2);
        moveDownCounts.put(Rank.SILVER_2, 2);
        moveDownCounts.put(Rank.SILVER_3, 2);
        moveDownCounts.put(Rank.GOLD_1, 2);
        moveDownCounts.put(Rank.GOLD_2, 2);
        moveDownCounts.put(Rank.GOLD_3, 2);
        moveDownCounts.put(Rank.PLATINUM_1, 2);
        moveDownCounts.put(Rank.PLATINUM_2, 2);
        moveDownCounts.put(Rank.PLATINUM_3, 2);
        moveDownCounts.put(Rank.DIAMOND_1, 2);
        moveDownCounts.put(Rank.DIAMOND_2, 2);
        moveDownCounts.put(Rank.DIAMOND_3, 2);
        moveDownCounts.put(Rank.MASTER, 2);
        moveDownCounts.put(Rank.GRANDMASTER, 2);

        return moveDownCounts;
    }
}