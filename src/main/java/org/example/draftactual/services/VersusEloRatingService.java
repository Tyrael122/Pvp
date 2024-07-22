package org.example.draftactual.services;

import lombok.Getter;
import org.example.draftactual.interfaces.EloRatingService;
import org.example.draftactual.model.Player;
import org.example.draftactual.model.Team;

import java.util.List;

public class VersusEloRatingService implements EloRatingService {

    private static final int K_FACTOR = 32;
    private static final int C_FACTOR = 400;

    @Override
    public List<Team> updateRatings(List<Team> teams, Team winner) {
        boolean isDraw = false;

        if (winner == null) {
            winner = teams.getFirst();

            isDraw = true;
        }

        Team loser = findLoser(teams, winner);

        double expectedOutcomeForWinner = calculateExpectedOutcome(winner.calculateAverageRating(), loser.calculateAverageRating());
        double expectedOutcomeForLoser = 1 - expectedOutcomeForWinner;

        updateRating(winner, expectedOutcomeForWinner, isDraw ? MatchOutcome.DRAW : MatchOutcome.WIN);
        updateRating(loser, expectedOutcomeForLoser, isDraw ? MatchOutcome.DRAW : MatchOutcome.LOSS);

        return teams;
    }

    private Team findLoser(List<Team> teams, Team winner) {
        return teams.stream().filter(team -> team != winner).findFirst().orElseThrow(() -> new IllegalArgumentException("Loser team not found."));
    }

    private void updateRating(Team team, double expectedOutcome, MatchOutcome matchOutcome) {
        double newRating = calculateNewRating(team.calculateAverageRating(), expectedOutcome, matchOutcome);

        for (Player player : team.getPlayers()) {
            player.setRating(newRating);
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
        return currentRating + K_FACTOR * (outcome.value - expectedOutcome);
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
