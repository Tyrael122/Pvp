package org.example.draftactual.interfaces;

import org.example.draftactual.model.Player;
import org.example.draftactual.model.Team;

import java.util.List;

public interface MatchmakingService {
    void queuePlayers(List<Player> players);
    void unqueuePlayers(List<Player> players);
    boolean isMatchReady();
    List<Team> fetchTeamsForMatch() throws IllegalStateException;
}