package org.example.pvp.matchmaking;

import org.example.pvp.interfaces.MatchmakingService;
import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchmakingProfile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VersusMatchmakingServiceTest {

    private int lastIdUsed = 0;

    private final MatchmakingService matchmakingService = new VersusMatchmakingService(3);

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
        return new MatchmakingProfile(lastIdUsed++, rating);
    }
}