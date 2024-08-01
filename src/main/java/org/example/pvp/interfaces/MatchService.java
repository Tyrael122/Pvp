package org.example.pvp.interfaces;

import org.example.pvp.model.Match;
import org.example.pvp.model.Player;
import org.example.pvp.model.Team;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchService {
    void startMatch(List<Team> teams);
    Match getPlayerMatch(Player player);
    List<Match> endMatchesReadyToEnd();
    LocalDateTime calculateNextMatchEndTime();
}