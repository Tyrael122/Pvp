package org.example.draftactual.services;

import org.example.draftactual.interfaces.RankingService;
import org.example.draftactual.model.Player;
import org.example.draftactual.model.Rank;

import java.util.*;

public class VersusRankingService implements RankingService {
    private final Map<Rank, Set<Player>> ranking = new HashMap<>();

    // TODO: Use a database. This is just for demonstration purposes. In the repository interface implementation, you can add some caching if needed.
    private final Map<Long, Player> playerCache = new HashMap<>();

    private final Map<Integer, Rank> rankLowerBoundaries;
    private final List<Integer> reversedLowerBoundaries;

    public VersusRankingService() {
        this(createRankLowerBoundaries());
    }

    public VersusRankingService(Map<Integer, Rank> rankLowerBoundaries) {
        this.rankLowerBoundaries = rankLowerBoundaries;
        this.reversedLowerBoundaries = rankLowerBoundaries.keySet().stream().sorted().toList().reversed();
    }

    @Override
    public void addPlayers(List<Player> players) {
        for (Player player : players) {
            savePlayer(player.clone());
        }

        updateRankings(players);
    }

    @Override
    public void removePlayers(List<Player> players) {
        for (Player player : players) {
            Player existingPlayer = findById(player.getId());

            if (existingPlayer.getRank() != null) {
                ranking.get(existingPlayer.getRank()).remove(existingPlayer);
            }

            removePlayer(player);
        }
    }

    @Override
    public void updateRankings(List<Player> players) {
        for (Player player : players) {
            Player playerWithOldRating = findById(player.getId());
            Player playerWithNewRating = player.clone();

            removeFromRanking(playerWithOldRating);

            Rank newRank = calculateRank(playerWithNewRating.getRating());
            addToRanking(playerWithNewRating, newRank);

            savePlayer(playerWithNewRating);
        }
    }

    @Override
    public List<Player> getRanking() {
        List<Player> result = new ArrayList<>();

        for (Rank rank : Rank.orderedRanks) {
            if (!ranking.containsKey(rank)) {
                continue;
            }

            result.addAll(ranking.get(rank));
        }

        return result;
    }

    @Override
    public List<Player> getRanking(Rank rank) {
        Set<Player> players = ranking.get(rank);
        if (players == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(players);
    }

    private void removeFromRanking(Player player) {
        if (player.getRank() == null) {
            return;
        }

        Set<Player> rankingSet = ranking.get(player.getRank());
        if (rankingSet == null) return;

        rankingSet.remove(player);
    }

    private void addToRanking(Player player, Rank newRank) {
        if (!ranking.containsKey(newRank)) {
            ranking.put(newRank, createSortedSet());
        }

        ranking.get(newRank).add(player);

        player.setRank(newRank);
    }

    private Player findById(long id) {
        return playerCache.get(id);
    }

    private void savePlayer(Player player) {
        playerCache.put(player.getId(), player);
    }

    private void removePlayer(Player player) {
        playerCache.remove(player.getId());
    }

    private Rank calculateRank(double rating) {
        for (Integer lowerBoundary : reversedLowerBoundaries) {
            if (rating >= lowerBoundary) {
                return rankLowerBoundaries.get(lowerBoundary);
            }
        }

        return Rank.GRANDMASTER;
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

    private Set<Player> createSortedSet() {
        return new TreeSet<>(Comparator.comparing(Player::getRating).reversed().thenComparing(Player::getId));
    }
}
