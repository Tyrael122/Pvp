package org.example.winnercalculator;

import org.example.pvp.interfaces.WinnerCalculator;
import org.example.pvp.model.MatchGroup;

import java.util.List;

public class FiftyFiftyRandomWinner implements WinnerCalculator {

    private static final int C_FACTOR = 150;

    @Override
    public MatchGroup calculateWinner(List<MatchGroup> matchGroups) {
        // Give more probability to the team with the highest rating.
        double expectedOutcome = calculateExpectedOutcome(matchGroups.getFirst().calculateAverageRating(), matchGroups.getLast().calculateAverageRating());

        if (Math.random() < expectedOutcome) {
            return matchGroups.getFirst();
        } else {
            return matchGroups.getLast();
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
