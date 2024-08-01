package org.example.pvp.matchmaking;

import org.example.pvp.interfaces.MatchmakingService;
import org.example.pvp.model.Player;
import org.example.pvp.model.Team;
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

        List<Team> teams = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, teams.size());
        assertEquals(3, teams.getFirst().getPlayers().size());
        assertEquals(3, teams.getLast().getPlayers().size());

        assertFalse(matchmakingService.isMatchReady());

        assertThrows(IllegalStateException.class, matchmakingService::fetchTeamsForMatch);
    }

    @Test
    void shouldForm3MatchesWith18PLayers() {
        queueNPlayers(18);

        assertTrue(matchmakingService.isMatchReady());

        List<Team> teams = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, teams.size());
        assertEquals(3, teams.getFirst().getPlayers().size());
        assertEquals(3, teams.getLast().getPlayers().size());

        teams = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, teams.size());
        assertEquals(3, teams.getFirst().getPlayers().size());
        assertEquals(3, teams.getLast().getPlayers().size());

        teams = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, teams.size());
        assertEquals(3, teams.getFirst().getPlayers().size());
        assertEquals(3, teams.getLast().getPlayers().size());

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
        Player player1 = createPlayer(500);
        matchmakingService.queuePlayers(List.of(player1));

        queueNPlayers(2, 500);
        queueNPlayers(3, 500);

        assertTrue(matchmakingService.isMatchReady());

        matchmakingService.unqueuePlayers(List.of(player1));

        assertFalse(matchmakingService.isMatchReady());

        matchmakingService.queuePlayers(List.of(player1));

        assertTrue(matchmakingService.isMatchReady());

        List<Team> teams = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, teams.size());
        assertEquals(3, teams.getFirst().getPlayers().size());
        assertEquals(3, teams.getLast().getPlayers().size());

        assertFalse(matchmakingService.isMatchReady());
    }

    @Test
    void shouldQueueAndUnqueuePlayerAndMatchShouldntStart() {
        queueNPlayers(5, 1000);

        assertFalse(matchmakingService.isMatchReady());

        Player player = createPlayer(1000);
        matchmakingService.queuePlayers(List.of(player));
        matchmakingService.unqueuePlayers(List.of(player));

        assertFalse(matchmakingService.isMatchReady());
    }

    private void queueNPlayers(int x) {
        queueNPlayers(x, 1000);
    }

    private void queueNPlayers(int x, double rating) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            players.add(createPlayer(rating));
        }

        matchmakingService.queuePlayers(players);
    }

    private Player createPlayer(double rating) {
        return new Player(lastIdUsed++, rating);
    }
}