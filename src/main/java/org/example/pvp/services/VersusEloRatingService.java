package org.example.pvp.services;

import lombok.Getter;
import org.example.pvp.interfaces.EloRatingService;
import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.stats.StatisticsService;

import java.util.List;

public class VersusEloRatingService implements EloRatingService {

    private static final int K_FACTOR = 100;
    private static final int C_FACTOR = 150;

    private final StatisticsService statisticsService;

    public VersusEloRatingService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Override
    public List<MatchGroup> updateRatings(List<MatchGroup> matchGroups, MatchGroup winner) {
        boolean isDraw = false;

        if (winner == null) {
            winner = matchGroups.getFirst();

            isDraw = true;
        }

        MatchGroup loser = findLoser(matchGroups, winner);

        double expectedOutcomeForWinner = calculateExpectedOutcome(winner.calculateAverageRating(), loser.calculateAverageRating());
        double expectedOutcomeForLoser = 1 - expectedOutcomeForWinner;

        double winnerAverageRating = winner.calculateAverageRating();
        double loserAverageRating = loser.calculateAverageRating();

        updateRating(winner, expectedOutcomeForWinner, isDraw ? MatchOutcome.DRAW : MatchOutcome.WIN);
        updateRating(loser, expectedOutcomeForLoser, isDraw ? MatchOutcome.DRAW : MatchOutcome.LOSS);

        statisticsService.addGainOfRating(winner.calculateAverageRating() - winnerAverageRating);
        statisticsService.addLossOfRating(loserAverageRating - loser.calculateAverageRating());

        return matchGroups;
    }

    private MatchGroup findLoser(List<MatchGroup> matchGroups, MatchGroup winner) {
        return matchGroups.stream().filter(team -> team != winner).findFirst().orElseThrow(() -> new IllegalArgumentException("Loser team not found."));
    }

    private void updateRating(MatchGroup matchGroup, double expectedOutcome, MatchOutcome matchOutcome) {
        double newRating = calculateNewRating(matchGroup.calculateAverageRating(), expectedOutcome, matchOutcome);

        for (MatchmakingProfile matchmakingProfile : matchGroup.getMatchmakingProfiles()) {
            matchmakingProfile.setRating(newRating);
        }
    }

    private double calculateExpectedOutcome(double ratingA, double ratingB) {
        double qForA = q(ratingA);
        double qForB = q(ratingB);

        return qForA / (qForA + qForB);
    }

    private double q(double rating) {
        return Math.pow(10, rating / C_FACTOR);
    }

    private double calculateNewRating(double currentRating, double expectedOutcome, MatchOutcome outcome) {
        double newRating = currentRating + K_FACTOR * (outcome.value - expectedOutcome);
        if (newRating < 0) {
            return 0;
        }

        return newRating;
    }

    @Getter
    private enum MatchOutcome {
        WIN(1),
        LOSS(0),
        DRAW(0.5);

        private final double value;

        MatchOutcome(double value) {
            this.value = value;
        }
    }
}
