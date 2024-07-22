package org.example.draftactual.matchmaking;

import org.example.draftactual.interfaces.MatchmakingService;
import org.example.draftactual.model.Player;
import org.example.draftactual.model.Team;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VersusMatchmakingServiceTest {

    private MatchmakingService matchmakingService = new VersusMatchmakingService();

    @Test
    void shouldPair6IntoTeamAndLeaveOneInQueue() {
        queueNPlayers(7);

        assertTrue(matchmakingService.isMatchReady());

        List<Team> teams = matchmakingService.fetchTeamsForMatch();
        assertEquals(2, teams.size());
        assertEquals(3, teams.getFirst().getPlayers().size());
        assertEquals(3, teams.getLast().getPlayers().size());

        assertFalse(matchmakingService.isMatchReady());

        assertThrows(IllegalStateException.class, () -> matchmakingService.fetchTeamsForMatch());
    }

    @Test
    void shouldForm3MatchesWith18PLayers() {
        queueNPlayers(18);

        assertTrue(matchmakingService.isMatchReady());

        List<Team> teams = matchmakingService.fetchTeamsForMatch();
        assertEquals(6, teams.size());
        assertEquals(3, teams.get(0).getPlayers().size());
        assertEquals(3, teams.get(1).getPlayers().size());
        assertEquals(3, teams.get(2).getPlayers().size());
        assertEquals(3, teams.get(3).getPlayers().size());
        assertEquals(3, teams.get(4).getPlayers().size());
        assertEquals(3, teams.get(5).getPlayers().size());

        assertFalse(matchmakingService.isMatchReady());

        assertThrows(IllegalStateException.class, () -> matchmakingService.fetchTeamsForMatch());
    }

    private void queueNPlayers(int x) {
        for (int i = 0; i < x; i++) {
            matchmakingService.queuePlayer(createPlayer(i));
        }
    }


    private Player createPlayer(int id) {
        Player player = new Player();
        player.setId(id);

        return player;
    }
}