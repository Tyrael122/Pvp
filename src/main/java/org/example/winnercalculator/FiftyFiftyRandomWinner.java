package org.example.winnercalculator;

import org.example.pvp.interfaces.WinnerCalculator;
import org.example.pvp.model.Team;

import java.util.List;

public class FiftyFiftyRandomWinner implements WinnerCalculator {

    private static final int C_FACTOR = 150;

    @Override
    public Team calculateWinner(List<Team> teams) {
        // Give more probability to the team with the highest rating.
        double expectedOutcome = calculateExpectedOutcome(teams.getFirst().calculateAverageRating(), teams.getLast().calculateAverageRating());

        if (Math.random() < expectedOutcome) {
            return teams.getFirst();
        } else {
            return teams.getLast();
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
}
