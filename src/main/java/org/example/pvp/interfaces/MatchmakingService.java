package org.example.pvp.interfaces;

import org.example.pvp.model.Player;
import org.example.pvp.model.Team;

import java.util.List;

public interface MatchmakingService {
    void queuePlayers(List<Player> players);
    void unqueuePlayers(List<Player> players);
    boolean isMatchReady();
    List<Team> fetchTeamsForMatch() throws IllegalStateException;
}