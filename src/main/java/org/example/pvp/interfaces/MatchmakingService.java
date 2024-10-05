package org.example.pvp.interfaces;

import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchmakingProfile;

import java.util.List;

public interface MatchmakingService {
    void queuePlayers(List<MatchmakingProfile> matchmakingProfiles);
    void unqueuePlayers(List<MatchmakingProfile> matchmakingProfiles);
    boolean isMatchReady();
    List<MatchGroup> fetchTeamsForMatch() throws IllegalStateException;
}