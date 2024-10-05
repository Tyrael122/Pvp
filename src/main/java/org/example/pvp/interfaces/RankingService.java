package org.example.pvp.interfaces;

import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.model.Division;

import java.util.List;

public interface RankingService {
    void addPlayers(List<MatchmakingProfile> matchmakingProfiles);
    void removePlayers(List<MatchmakingProfile> matchmakingProfiles);
    void updateRankings(List<MatchmakingProfile> matchmakingProfiles);
    List<MatchmakingProfile> getRanking();
    List<MatchmakingProfile> getRanking(Division division);
}