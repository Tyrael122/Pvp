package org.example.pvp.services;

import org.example.pvp.interfaces.RankingService;
import org.example.pvp.model.Division;
import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.repositories.MatchmakingProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VersusRankingService implements RankingService {
    private final Map<Division, List<MatchmakingProfile>> ranking = new HashMap<>();
    private boolean areRanksSorted = false;

    private final Map<Integer, Division> rankLowerBoundaries;
    private final List<Integer> reversedLowerBoundaries;

    private final int MAX_BEST_RANK_SIZE = 10;

    private MatchmakingProfile weakestPlayerRankInBestRanking = null;

    private final MatchmakingProfileRepository matchmakingProfileRepository;

    @Autowired
    public VersusRankingService(MatchmakingProfileRepository matchmakingProfileRepository) {
        this(createRankLowerBoundaries(), matchmakingProfileRepository);
    }

    public VersusRankingService(Map<Integer, Division> rankLowerBoundaries, MatchmakingProfileRepository matchmakingProfileRepository) {
        this.rankLowerBoundaries = rankLowerBoundaries;
        this.reversedLowerBoundaries = rankLowerBoundaries.keySet().stream().sorted().toList().reversed();
        this.matchmakingProfileRepository = matchmakingProfileRepository;

        initializeRankings();
    }

    @Override
    public void addPlayers(List<MatchmakingProfile> matchmakingProfiles) {
        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            saveMatchmakingProfile(matchmakingProfile);
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

            // This method has the semantics of removing the player from the ranking, not deleting his profile. This is wrong.
            // In the current implementation, this is necessary so as not to load him again in the next fetchRanking call,
            // but it could be worked around by adding a flag to the profile, or by storing the deleted profiles in a separate table.
//            removeMatchmakingProfile(matchmakingProfile);
        }
    }

    @Override
    public void updateRankings(List<MatchmakingProfile> matchmakingProfiles) {
        areRanksSorted = false;

        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            MatchmakingProfile matchmakingProfileWithOldRank = findById(matchmakingProfile.getId());
            removeFromRanking(matchmakingProfileWithOldRank);

            Division matchedDivision = calculateRank(matchmakingProfile.getRating());
            addToRanking(matchmakingProfile, matchedDivision);

            saveMatchmakingProfile(matchmakingProfile);
        }
    }

    @Override
    public List<MatchmakingProfile> getRanking() {
        sortRanksIfNeeded();

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
        sortRanksIfNeeded();

        List<MatchmakingProfile> matchmakingProfiles = ranking.get(division);
        if (matchmakingProfiles == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(matchmakingProfiles);
    }

    private void removeFromRanking(MatchmakingProfile matchmakingProfile) {
        if (matchmakingProfile.getDivision() == null) {
            return;
        }

        List<MatchmakingProfile> rankingSet = ranking.get(matchmakingProfile.getDivision());
        if (rankingSet == null) return;

        for (MatchmakingProfile profile : rankingSet) {
            if (profile.getId() == matchmakingProfile.getId()) {
                rankingSet.remove(profile);
                break;
            }
        }
    }

    private void addToRanking(MatchmakingProfile matchmakingProfile, Division division) {
        if (!ranking.containsKey(division)) {
            ranking.put(division, new ArrayList<>());
        }

        matchmakingProfile.setDivision(division);

        if (division == Division.ASCENDING_DIVISIONS.getLast()) {
            handlePlayerInBestRank(matchmakingProfile);
        }

        ranking.get(division).add(matchmakingProfile);
    }

    private MatchmakingProfile findById(long id) {
        return matchmakingProfileRepository.findById(id).orElseThrow();
    }

    private void saveMatchmakingProfile(MatchmakingProfile matchmakingProfile) {
        matchmakingProfileRepository.save(matchmakingProfile);
    }

    private void removeMatchmakingProfile(MatchmakingProfile matchmakingProfile) {
        matchmakingProfileRepository.deleteById(matchmakingProfile.getId());
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
        List<MatchmakingProfile> bestMatchmakingProfiles = ranking.get(Division.ASCENDING_DIVISIONS.getLast());
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

        List<MatchmakingProfile> bestMatchmakingProfiles = ranking.get(Division.ASCENDING_DIVISIONS.getLast());
        if (bestMatchmakingProfiles == null || bestMatchmakingProfiles.isEmpty() || bestMatchmakingProfiles.size() < MAX_BEST_RANK_SIZE) {
            return true;
        }

        return rating > weakestPlayerRankInBestRanking.getRating();
    }

    private void initializeRankings() {
        List<MatchmakingProfile> matchmakingProfiles = matchmakingProfileRepository.findAll();

        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            Division division = matchmakingProfile.getDivision();

            if (!ranking.containsKey(division)) {
                ranking.put(division, new ArrayList<>());
            }

            ranking.get(division).add(matchmakingProfile);
        }
    }

    private void sortRanksIfNeeded() {
        if (!areRanksSorted) {
            sortRanks();

            areRanksSorted = true;
        }
    }

    private void sortRanks() {
        for (Division division : Division.ASCENDING_DIVISIONS) {
            List<MatchmakingProfile> matchmakingProfiles = ranking.get(division);
            if (matchmakingProfiles == null) {
                continue;
            }

            matchmakingProfiles.sort(Comparator.comparing(MatchmakingProfile::getRating).reversed());
        }
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
}
