package org.example.draftactual.interfaces;

import org.example.draftactual.model.Player;
import org.example.draftactual.model.Team;

import java.util.List;

public interface MatchmakingService {
    void queuePlayer(Player player); // TODO: Make this accept a list of players.
    void unqueuePlayer(Player player); // TODO: Make this accept a list of players.
    boolean isMatchReady();
    List<Team> fetchTeamsForMatch() throws IllegalStateException;
}