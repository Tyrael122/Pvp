package org.example.pvp.matchmaking;

import jakarta.transaction.Transactional;
import org.example.pvp.interfaces.MatchmakingService;
import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.repositories.MatchmakingProfileRepository;
import org.example.pvp.stats.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@Transactional
@SpringBootTest
class VersusMatchmakingServiceTest {

    @Autowired
    private MatchmakingProfileRepository matchmakingProfileRepository;

    @Autowired
    private WaitingTeamRepository waitingTeamRepository;

    private MatchmakingService matchmakingService;

    @BeforeEach
    void setUp() {
        matchmakingService = createNewMatchmakingService();
    }

    @Test
    void shouldPair6IntoTeamAndLeaveOneInQueue() {
        queueNPlayers(7);

        assertTrue(matchmakingService.isMatchReady());

        List<MatchGroup> matchGroups = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, matchGroups.size());
        assertEquals(3, matchGroups.getFirst().getMatchmakingProfiles().size());
        assertEquals(3, matchGroups.getLast().getMatchmakingProfiles().size());

        assertFalse(matchmakingService.isMatchReady());

        assertThrows(IllegalStateException.class, matchmakingService::fetchTeamsForMatch);
    }

    @Test
    void shouldForm3MatchesWith18PLayers() {
        queueNPlayers(18);

        assertTrue(matchmakingService.isMatchReady());

        List<MatchGroup> matchGroups = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, matchGroups.size());
        assertEquals(3, matchGroups.getFirst().getMatchmakingProfiles().size());
        assertEquals(3, matchGroups.getLast().getMatchmakingProfiles().size());

        matchGroups = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, matchGroups.size());
        assertEquals(3, matchGroups.getFirst().getMatchmakingProfiles().size());
        assertEquals(3, matchGroups.getLast().getMatchmakingProfiles().size());

        matchGroups = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, matchGroups.size());
        assertEquals(3, matchGroups.getFirst().getMatchmakingProfiles().size());
        assertEquals(3, matchGroups.getLast().getMatchmakingProfiles().size());

        assertFalse(matchmakingService.isMatchReady());

        assertThrows(IllegalStateException.class, matchmakingService::fetchTeamsForMatch);
    }

    @Test
    void shouldNotStartMatchWhenPlayersAreTooFarApart() {
        queueNPlayers(3, 500);
        queueNPlayers(3, 3000);

        assertFalse(matchmakingService.isMatchReady());

        assertThrows(IllegalStateException.class, matchmakingService::fetchTeamsForMatch);
    }

    @Test
    void shouldStartMatchWhenUnqueuingPlayerAndQueuingAgain() {
        MatchmakingProfile matchmakingProfile1 = createPlayer(500);
        matchmakingService.queuePlayers(List.of(matchmakingProfile1));

        queueNPlayers(2, 500);
        queueNPlayers(3, 500);

        assertTrue(matchmakingService.isMatchReady());

        matchmakingService.unqueuePlayers(List.of(matchmakingProfile1));

        assertFalse(matchmakingService.isMatchReady());

        matchmakingService.queuePlayers(List.of(matchmakingProfile1));

        assertTrue(matchmakingService.isMatchReady());

        List<MatchGroup> matchGroups = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, matchGroups.size());
        assertEquals(3, matchGroups.getFirst().getMatchmakingProfiles().size());
        assertEquals(3, matchGroups.getLast().getMatchmakingProfiles().size());

        assertFalse(matchmakingService.isMatchReady());
    }

    @Test
    void shouldQueueAndUnqueuePlayerAndMatchShouldntStart() {
        queueNPlayers(5, 1000);

        assertFalse(matchmakingService.isMatchReady());

        MatchmakingProfile matchmakingProfile = createPlayer(1000);
        matchmakingService.queuePlayers(List.of(matchmakingProfile));
        matchmakingService.unqueuePlayers(List.of(matchmakingProfile));

        assertFalse(matchmakingService.isMatchReady());
    }

    @Test
    void afterQueuingPlayersAndRestartingServiceShouldRetrieveThemCorrectly() {
        queueNPlayers(5, 1000);

        assertFalse(matchmakingService.isMatchReady());

        matchmakingService = createNewMatchmakingService();

        assertFalse(matchmakingService.isMatchReady());

        queueNPlayers(1, 1000);

        assertTrue(matchmakingService.isMatchReady());

        List<MatchGroup> matchGroups = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, matchGroups.size());
        assertEquals(3, matchGroups.getFirst().getMatchmakingProfiles().size());
        assertEquals(3, matchGroups.getLast().getMatchmakingProfiles().size());

        assertFalse(matchmakingService.isMatchReady());
    }

    private void queueNPlayers(int x) {
        queueNPlayers(x, 1000);
    }

    private void queueNPlayers(int x, double rating) {
        List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            matchmakingProfiles.add(createPlayer(rating));
        }

        matchmakingService.queuePlayers(matchmakingProfiles);
    }

    private MatchmakingProfile createPlayer(double rating) {
        var matchmakingProfile = new MatchmakingProfile(rating);
        return matchmakingProfileRepository.save(matchmakingProfile);
    }

    private MatchmakingService createNewMatchmakingService() {
        return new VersusMatchmakingService(3, mock(StatisticsService.class), waitingTeamRepository);
    }
}