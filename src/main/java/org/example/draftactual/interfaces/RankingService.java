package org.example.draftactual.interfaces;

import org.example.draftactual.model.Player;
import org.example.draftactual.model.Rank;

import java.util.List;

public interface RankingService {
    void addPlayers(List<Player> players);
    void removePlayers(List<Player> players);
    void updateRankings(List<Player> players);
    List<Player> getRanking();
    List<Player> getRanking(Rank rank);
}