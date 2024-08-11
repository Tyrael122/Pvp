package org.example.pvp.services;

import jakarta.transaction.Transactional;
import org.example.pvp.interfaces.EloRatingService;
import org.example.pvp.interfaces.RankingService;
import org.example.pvp.interfaces.WinnerCalculator;
import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.repositories.MatchRepository;
import org.example.pvp.repositories.MatchmakingProfileRepository;
import org.example.winnercalculator.FiftyFiftyRandomWinner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@Transactional
@SpringBootTest
class VersusMatchServiceTest {

    private final WinnerCalculator winnerCalculator = new FiftyFiftyRandomWinner();

    @Autowired
    private MatchmakingProfileRepository matchmakingProfileRepository;

    @Autowired
    private MatchRepository matchRepository;

    private VersusMatchService versusMatchService;

    @BeforeEach
    void setUp() {
        versusMatchService = createVersusMatchService();
    }

    @Test
    void shouldEndMatchAfterControllerRestart() {
        MatchmakingProfile profile1 = createMatchmakingProfile();
        MatchmakingProfile profile2 = createMatchmakingProfile();

        MatchGroup matchGroup = MatchGroup.of(List.of(profile1, profile2));

        versusMatchService.startMatch(List.of(matchGroup));

        versusMatchService = createVersusMatchService();

        assertFalse(versusMatchService.endMatchesReadyToEnd().isEmpty());
    }

    public VersusMatchService createVersusMatchService() {
        return new VersusMatchService(winnerCalculator, mock(EloRatingService.class), mock(RankingService.class), matchRepository, () -> LocalDateTime.now().minusMinutes(1));
    }

    public MatchmakingProfile createMatchmakingProfile() {
        MatchmakingProfile matchmakingProfile = new MatchmakingProfile();
        matchmakingProfileRepository.save(matchmakingProfile);

        return matchmakingProfile;
    }
}