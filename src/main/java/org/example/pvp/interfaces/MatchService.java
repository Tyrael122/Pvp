package org.example.pvp.interfaces;

import org.example.pvp.model.Match;
import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchmakingProfile;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchService {
    void startMatch(List<MatchGroup> matchGroups);
    Match getPlayerMatch(MatchmakingProfile matchmakingProfile);
    List<Match> endMatchesReadyToEnd();
    LocalDateTime calculateNextMatchEndTime();
}