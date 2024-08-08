package org.example.pvp.stats;

import org.example.pvp.interfaces.RankingService;
import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.model.Division;
import org.example.pvp.model.MatchGroup;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Make this thing implement an interface to clients who will query for it's stats.
@Service
public class StatisticsService {

    private double sumOfDifferenceInRatingBetweenTeams = 0;
    private long numberOfMatchesFormed = 0;

    private double numberOfMatchesFinished = 0;
    private double sumOfRatingGained = 0;
    private double sumOfRatingLost = 0;

    private final RankingService rankingService;

    public StatisticsService(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    public void addFormedMatch(List<MatchGroup> matchGroups) {
        sumOfDifferenceInRatingBetweenTeams = Math.abs(matchGroups.getFirst().calculateAverageRating() - matchGroups.getLast().calculateAverageRating());
        numberOfMatchesFormed++;
    }

    public Map<Object, Object> getStatistics() {
        Map<Division, Integer> rankingsSize = new HashMap<>();

        for (Division division : Division.ASCENDING_DIVISIONS) {
            List<MatchmakingProfile> ranking = rankingService.getRanking(division);
            if (!ranking.isEmpty()) {
                rankingsSize.put(division, ranking.size());
            }
        }

        double averageRating = rankingService.getRanking().stream().mapToDouble(MatchmakingProfile::getRating).average().orElse(0.0);

        double averageDifferenceInRatingBetweenTeams = getAverageDifferenceInRatingBetweenTeams();

        Map<Object, Object> statistics = new HashMap<>();

        statistics.put("bestPlayer", rankingService.getRanking().stream().max(Comparator.comparingDouble(MatchmakingProfile::getRating)).orElse(null));
        statistics.put("worstPlayer", rankingService.getRanking().stream().min(Comparator.comparingDouble(MatchmakingProfile::getRating)).orElse(null));
        statistics.put("rankingsSize", rankingsSize);
        statistics.put("averageRating", averageRating);
        statistics.put("averageDifferenceInRatingBetweenTeams", averageDifferenceInRatingBetweenTeams);

        if (numberOfMatchesFinished > 0) {
            statistics.put("averageRatingGained", sumOfRatingGained / numberOfMatchesFinished);
            statistics.put("averageRatingLost", sumOfRatingLost / numberOfMatchesFinished);
            statistics.put("numberOfMatchesFinished", numberOfMatchesFinished);
        }

        return statistics;
    }

    private double getAverageDifferenceInRatingBetweenTeams() {
        if (numberOfMatchesFormed == 0) {
            return 0;
        }

        return sumOfDifferenceInRatingBetweenTeams / numberOfMatchesFormed;
    }

    public void addGainOfRating(double v) {
        sumOfRatingGained += v;
        numberOfMatchesFinished++;
    }

    public void addLossOfRating(double v) {
        sumOfRatingLost += v;
    }
}
