package org.example.pvp.services;

import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.repositories.MatchmakingProfileRepository;
import org.example.pvp.stats.StatisticsService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class VersusEloRatingServiceTest {
    private final VersusEloRatingService eloRatingService = new VersusEloRatingService(mock(StatisticsService.class), mock(MatchmakingProfileRepository.class));

    @Test
    void shouldDistributeRatingsToWinnerAndLoser() {
        double initialRating = 1500;

        MatchGroup matchGroup1 = MatchGroup.of(List.of(createPlayer(initialRating)));
        MatchGroup matchGroup2 = MatchGroup.of(List.of(createPlayer(initialRating)));

        List<MatchGroup> updatedMatchGroups = eloRatingService.updateRatings(List.of(matchGroup1, matchGroup2), matchGroup1);

        assertTrue(updatedMatchGroups.get(0).calculateAverageRating() > initialRating);
        assertTrue(updatedMatchGroups.get(1).calculateAverageRating() < initialRating);
    }

    @Test
    void equalRatingsDrawShouldNotChangeRatings() {
        double initialRating = 1500;

        MatchGroup matchGroup1 = MatchGroup.of(List.of(createPlayer(initialRating)));
        MatchGroup matchGroup2 = MatchGroup.of(List.of(createPlayer(initialRating)));

        List<MatchGroup> updatedMatchGroups = eloRatingService.updateRatings(List.of(matchGroup1, matchGroup2), null);

        assertEquals(initialRating, updatedMatchGroups.get(0).calculateAverageRating());
        assertEquals(initialRating, updatedMatchGroups.get(1).calculateAverageRating());
    }

    @Test
    void differentRatingsDrawShouldChangeRatings() {
        double initialRating1 = 1500;
        double initialRating2 = 1600;

        MatchGroup matchGroup1 = MatchGroup.of(List.of(createPlayer(initialRating1)));
        MatchGroup matchGroup2 = MatchGroup.of(List.of(createPlayer(initialRating2)));

        List<MatchGroup> updatedMatchGroups = eloRatingService.updateRatings(List.of(matchGroup1, matchGroup2), null);

        assertTrue(updatedMatchGroups.get(0).calculateAverageRating() > initialRating1);
        assertTrue(updatedMatchGroups.get(1).calculateAverageRating() < initialRating2);
    }

    private MatchmakingProfile createPlayer(double rating) {
        return new MatchmakingProfile(rating);
    }
}