package org.example.draftactual.interfaces;

import org.example.draftactual.model.Match;
import org.example.draftactual.model.Player;
import org.example.draftactual.model.Team;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchService {
    void startMatch(List<Team> teams);
    Match getPlayerMatch(Player player);
    List<Match> endMatchesReadyToEnd();
    LocalDateTime calculateNextMatchEndTime();
}