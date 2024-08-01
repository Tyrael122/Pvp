package org.example.pvp.services;

import org.example.pvp.interfaces.RankingService;
import org.example.pvp.model.Player;
import org.example.pvp.model.Rank;

import java.util.*;

public class VersusRankingService implements RankingService {
    private final Map<Rank, Set<Player>> ranking = new HashMap<>();

    // TODO: Use a database. This is just for demonstration purposes. In the repository interface implementation, you can add some caching if needed.
    private final Map<Long, Player> playerCache = new HashMap<>();

    private final Map<Integer, Rank> rankLowerBoundaries;
    private final List<Integer> reversedLowerBoundaries;

    private final int MAX_BEST_RANK_SIZE = 10;

    private Player weakestPlayerInBestRank = null;

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

            Rank matchedRank = calculateRank(playerWithNewRating.getRating());
            addToRanking(playerWithNewRating, matchedRank);

            savePlayer(playerWithNewRating);
        }
    }

    @Override
    public List<Player> getRanking() {
        List<Player> result = new ArrayList<>();

        for (Rank rank : Rank.ascendingRanks.reversed()) {
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

    private void addToRanking(Player player, Rank rank) {
        if (!ranking.containsKey(rank)) {
            ranking.put(rank, createSortedSet());
        }

        player.setRank(rank);

        if (rank == Rank.ascendingRanks.getLast()) {
            handlePlayerInBestRank(player);
        }

        ranking.get(rank).add(player);
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
                Rank matchedRank = rankLowerBoundaries.get(lowerBoundary);

                if (canPromoteToBestRank(rating, matchedRank)) {
                    return Rank.ascendingRanks.getLast();
                }

                return matchedRank;
            }
        }

        throw new IllegalStateException("Rank not found for rating: " + rating);
    }

    private void handlePlayerInBestRank(Player player) {
        Set<Player> bestPlayers = ranking.get(Rank.ascendingRanks.getLast());
        if (bestPlayers == null || bestPlayers.isEmpty()) {
            weakestPlayerInBestRank = player;

            return;
        }

        if (bestPlayers.size() < MAX_BEST_RANK_SIZE) {
            weakestPlayerInBestRank = bestPlayers.stream().min(Comparator.comparing(Player::getRating)).orElse(player);

            return;
        }

        bestPlayers.remove(weakestPlayerInBestRank);
        bestPlayers.add(player);

        Rank rankBeforeBest = Rank.ascendingRanks.get(Rank.ascendingRanks.size() - 2);
        addToRanking(weakestPlayerInBestRank, rankBeforeBest);

        weakestPlayerInBestRank = bestPlayers.stream().min(Comparator.comparing(Player::getRating)).orElse(player);
    }

    private boolean canPromoteToBestRank(double rating, Rank matchedRank) {
        Rank rankBeforeLast = Rank.ascendingRanks.get(Rank.ascendingRanks.size() - 2);
        if (matchedRank != rankBeforeLast) {
            return false;
        }

        Set<Player> bestPlayers = ranking.get(Rank.ascendingRanks.getLast());
        if (bestPlayers == null || bestPlayers.isEmpty() || bestPlayers.size() < MAX_BEST_RANK_SIZE) {
            return true;
        }

        return rating > weakestPlayerInBestRank.getRating();
    }

    private static Map<Integer, Rank> createRankLowerBoundaries() {
        Map<Integer, Rank> rankBoundaries = new HashMap<>();

        rankBoundaries.put(0, Rank.BRONZE_1);
        rankBoundaries.put(50, Rank.BRONZE_2);
        rankBoundaries.put(100, Rank.SILVER_1);
        rankBoundaries.put(150, Rank.SILVER_2);
        rankBoundaries.put(200, Rank.GOLD_1);
        rankBoundaries.put(250, Rank.GOLD_2);
        rankBoundaries.put(300, Rank.PLATINUM_1);
        rankBoundaries.put(350, Rank.PLATINUM_2);
        rankBoundaries.put(400, Rank.MASTER);

        return rankBoundaries;
    }

    private Set<Player> createSortedSet() {
        return new TreeSet<>(Comparator.comparing(Player::getRating).reversed().thenComparing(Player::getId));
    }
}
