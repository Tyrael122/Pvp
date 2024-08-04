package org.example.pvp.services;

import org.example.pvp.interfaces.RankingService;
import org.example.pvp.model.Division;
import org.example.pvp.model.MatchmakingProfile;

import java.util.*;

public class VersusRankingService implements RankingService {
    private final Map<Division, Set<MatchmakingProfile>> ranking = new HashMap<>();

    // TODO: Use a database. This is just for demonstration purposes. In the repository interface implementation, you can add some caching if needed.
    private final Map<Long, MatchmakingProfile> playerCache = new HashMap<>();

    private final Map<Integer, Division> rankLowerBoundaries;
    private final List<Integer> reversedLowerBoundaries;

    private final int MAX_BEST_RANK_SIZE = 10;

    private MatchmakingProfile weakestPlayerRankInBestRanking = null;

    public VersusRankingService() {
        this(createRankLowerBoundaries());
    }

    public VersusRankingService(Map<Integer, Division> rankLowerBoundaries) {
        this.rankLowerBoundaries = rankLowerBoundaries;
        this.reversedLowerBoundaries = rankLowerBoundaries.keySet().stream().sorted().toList().reversed();
    }

    @Override
    public void addPlayers(List<MatchmakingProfile> matchmakingProfiles) {
        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            savePlayer(matchmakingProfile.clone());
        }

        updateRankings(matchmakingProfiles);
    }

    @Override
    public void removePlayers(List<MatchmakingProfile> matchmakingProfiles) {
        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            MatchmakingProfile existingMatchmakingProfile = findById(matchmakingProfile.getId());

            if (existingMatchmakingProfile.getDivision() != null) {
                ranking.get(existingMatchmakingProfile.getDivision()).remove(existingMatchmakingProfile);
            }

            removePlayer(matchmakingProfile);
        }
    }

    @Override
    public void updateRankings(List<MatchmakingProfile> matchmakingProfiles) {
        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            MatchmakingProfile matchmakingProfileWithOldRating = findById(matchmakingProfile.getId());
            MatchmakingProfile matchmakingProfileWithNewRating = matchmakingProfile.clone();

            removeFromRanking(matchmakingProfileWithOldRating);

            Division matchedDivision = calculateRank(matchmakingProfileWithNewRating.getRating());
            addToRanking(matchmakingProfileWithNewRating, matchedDivision);

            savePlayer(matchmakingProfileWithNewRating);
        }
    }

    @Override
    public List<MatchmakingProfile> getRanking() {
        List<MatchmakingProfile> result = new ArrayList<>();

        for (Division division : Division.ASCENDING_DIVISIONS.reversed()) {
            if (!ranking.containsKey(division)) {
                continue;
            }

            result.addAll(ranking.get(division));
        }

        return result;
    }

    @Override
    public List<MatchmakingProfile> getRanking(Division division) {
        Set<MatchmakingProfile> matchmakingProfiles = ranking.get(division);
        if (matchmakingProfiles == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(matchmakingProfiles);
    }

    private void removeFromRanking(MatchmakingProfile matchmakingProfile) {
        if (matchmakingProfile.getDivision() == null) {
            return;
        }

        Set<MatchmakingProfile> rankingSet = ranking.get(matchmakingProfile.getDivision());
        if (rankingSet == null) return;

        rankingSet.remove(matchmakingProfile);
    }

    private void addToRanking(MatchmakingProfile matchmakingProfile, Division division) {
        if (!ranking.containsKey(division)) {
            ranking.put(division, createSortedSet());
        }

        matchmakingProfile.setDivision(division);

        if (division == Division.ASCENDING_DIVISIONS.getLast()) {
            handlePlayerInBestRank(matchmakingProfile);
        }

        ranking.get(division).add(matchmakingProfile);
    }

    private MatchmakingProfile findById(long id) {
        return playerCache.get(id);
    }

    private void savePlayer(MatchmakingProfile matchmakingProfile) {
        playerCache.put(matchmakingProfile.getId(), matchmakingProfile);
    }

    private void removePlayer(MatchmakingProfile matchmakingProfile) {
        playerCache.remove(matchmakingProfile.getId());
    }

    private Division calculateRank(double rating) {
        for (Integer lowerBoundary : reversedLowerBoundaries) {
            if (rating >= lowerBoundary) {
                Division matchedDivision = rankLowerBoundaries.get(lowerBoundary);

                if (canPromoteToBestRank(rating, matchedDivision)) {
                    return Division.ASCENDING_DIVISIONS.getLast();
                }

                return matchedDivision;
            }
        }

        throw new IllegalStateException("Rank not found for rating: " + rating);
    }

    private void handlePlayerInBestRank(MatchmakingProfile matchmakingProfile) {
        Set<MatchmakingProfile> bestMatchmakingProfiles = ranking.get(Division.ASCENDING_DIVISIONS.getLast());
        if (bestMatchmakingProfiles == null || bestMatchmakingProfiles.isEmpty()) {
            weakestPlayerRankInBestRanking = matchmakingProfile;

            return;
        }

        if (bestMatchmakingProfiles.size() < MAX_BEST_RANK_SIZE) {
            weakestPlayerRankInBestRanking = bestMatchmakingProfiles.stream().min(Comparator.comparing(MatchmakingProfile::getRating)).orElse(matchmakingProfile);

            return;
        }

        bestMatchmakingProfiles.remove(weakestPlayerRankInBestRanking);
        bestMatchmakingProfiles.add(matchmakingProfile);

        Division divisionBeforeBest = Division.ASCENDING_DIVISIONS.get(Division.ASCENDING_DIVISIONS.size() - 2);
        addToRanking(weakestPlayerRankInBestRanking, divisionBeforeBest);

        weakestPlayerRankInBestRanking = bestMatchmakingProfiles.stream().min(Comparator.comparing(MatchmakingProfile::getRating)).orElse(matchmakingProfile);
    }

    private boolean canPromoteToBestRank(double rating, Division matchedDivision) {
        Division divisionBeforeLast = Division.ASCENDING_DIVISIONS.get(Division.ASCENDING_DIVISIONS.size() - 2);
        if (matchedDivision != divisionBeforeLast) {
            return false;
        }

        Set<MatchmakingProfile> bestMatchmakingProfiles = ranking.get(Division.ASCENDING_DIVISIONS.getLast());
        if (bestMatchmakingProfiles == null || bestMatchmakingProfiles.isEmpty() || bestMatchmakingProfiles.size() < MAX_BEST_RANK_SIZE) {
            return true;
        }

        return rating > weakestPlayerRankInBestRanking.getRating();
    }

    private static Map<Integer, Division> createRankLowerBoundaries() {
        Map<Integer, Division> rankBoundaries = new HashMap<>();

        rankBoundaries.put(0, Division.BRONZE_1);
        rankBoundaries.put(50, Division.BRONZE_2);
        rankBoundaries.put(100, Division.SILVER_1);
        rankBoundaries.put(150, Division.SILVER_2);
        rankBoundaries.put(200, Division.GOLD_1);
        rankBoundaries.put(250, Division.GOLD_2);
        rankBoundaries.put(300, Division.PLATINUM_1);
        rankBoundaries.put(350, Division.PLATINUM_2);
        rankBoundaries.put(400, Division.MASTER);

        return rankBoundaries;
    }

    private Set<MatchmakingProfile> createSortedSet() {
        return new TreeSet<>(Comparator.comparing(MatchmakingProfile::getRating).reversed().thenComparing(MatchmakingProfile::getId));
    }
}
