package org.example.pvp.services;

import org.example.pvp.model.Player;
import org.example.pvp.model.Team;
import org.example.pvp.stats.StatisticsService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class VersusEloRatingServiceTest {
    private final VersusEloRatingService eloRatingService = new VersusEloRatingService(mock(StatisticsService.class));

    @Test
    void shouldDistributeRatingsToWinnerAndLoser() {
        double initialRating = 1500;

        Team team1 = Team.of(List.of(createPlayer(initialRating)));
        Team team2 = Team.of(List.of(createPlayer(initialRating)));

        List<Team> updatedTeams = eloRatingService.updateRatings(List.of(team1, team2), team1);

        assertTrue(updatedTeams.get(0).calculateAverageRating() > initialRating);
        assertTrue(updatedTeams.get(1).calculateAverageRating() < initialRating);
    }

    @Test
    void equalRatingsDrawShouldNotChangeRatings() {
        double initialRating = 1500;

        Team team1 = Team.of(List.of(createPlayer(initialRating)));
        Team team2 = Team.of(List.of(createPlayer(initialRating)));

        List<Team> updatedTeams = eloRatingService.updateRatings(List.of(team1, team2), null);

        assertEquals(initialRating, updatedTeams.get(0).calculateAverageRating());
        assertEquals(initialRating, updatedTeams.get(1).calculateAverageRating());
    }

    @Test
    void differentRatingsDrawShouldChangeRatings() {
        double initialRating1 = 1500;
        double initialRating2 = 1600;

        Team team1 = Team.of(List.of(createPlayer(initialRating1)));
        Team team2 = Team.of(List.of(createPlayer(initialRating2)));

        List<Team> updatedTeams = eloRatingService.updateRatings(List.of(team1, team2), null);

        assertTrue(updatedTeams.get(0).calculateAverageRating() > initialRating1);
        assertTrue(updatedTeams.get(1).calculateAverageRating() < initialRating2);
    }

    private Player createPlayer(double rating) {
        Player player = new Player(0);
        player.setRating(rating);

        return player;
    }
}