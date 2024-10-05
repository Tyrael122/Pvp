package org.example.pvp.services;

import org.example.pvp.model.Division;
import org.example.pvp.model.MatchmakingProfile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("Disabled until the WeeklyRankingService is properly implemented")
public class WeeklyRankingServiceTest {

    private final WeeklyRankingService rankingService = new WeeklyRankingService();

    @Test
    public void testMoveUpAndDown() {
        List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();
        matchmakingProfiles.add(new MatchmakingProfile(0, Division.BRONZE_2, 1200));
        matchmakingProfiles.add(new MatchmakingProfile(1, Division.BRONZE_2, 1100));
        matchmakingProfiles.add(new MatchmakingProfile(2, Division.BRONZE_2, 1000));

        matchmakingProfiles.add(new MatchmakingProfile(3, Division.BRONZE_1, 820));
        matchmakingProfiles.add(new MatchmakingProfile(4, Division.BRONZE_1, 810));
        matchmakingProfiles.add(new MatchmakingProfile(5, Division.BRONZE_1, 800));

        rankingService.addPlayers(matchmakingProfiles);
        rankingService.rankPlayers();

        List<MatchmakingProfile> bronze1Ranking = rankingService.getRanking(Division.BRONZE_1);
        assertEquals(1, bronze1Ranking.get(0).getId());
        assertEquals(2, bronze1Ranking.get(1).getId());

        List<MatchmakingProfile> bronze2Ranking = rankingService.getRanking(Division.BRONZE_2);
        assertEquals(3, bronze2Ranking.get(1).getId());
        assertEquals(4, bronze2Ranking.get(2).getId());
    }

    @Test
    public void testMoveOnlyUpWhenUpperDoesntHaveEnoughPlayersToMoveDown() {
        List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();
        matchmakingProfiles.add(new MatchmakingProfile(0, Division.BRONZE_1, 1200));
        matchmakingProfiles.add(new MatchmakingProfile(1, Division.BRONZE_1, 1100));
        matchmakingProfiles.add(new MatchmakingProfile(2, Division.BRONZE_1, 1000));

        rankingService.addPlayers(matchmakingProfiles);
        rankingService.rankPlayers();

        List<MatchmakingProfile> bronze1Ranking = rankingService.getRanking(Division.BRONZE_1);
        assertEquals(1, bronze1Ranking.size());
        assertEquals(2, bronze1Ranking.get(0).getId());

        List<MatchmakingProfile> bronze2Ranking = rankingService.getRanking(Division.BRONZE_2);
        assertEquals(2, bronze2Ranking.size());
        assertEquals(0, bronze2Ranking.get(0).getId());
        assertEquals(1, bronze2Ranking.get(1).getId());
    }

    @Test
    public void testMoveOnlyDownWhenLowerDoesntHaveEnoughPlayersToMoveUp() {
        List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();
        matchmakingProfiles.add(new MatchmakingProfile(0, Division.GOLD_1, 1200));
        matchmakingProfiles.add(new MatchmakingProfile(1, Division.GOLD_1, 1180));
        matchmakingProfiles.add(new MatchmakingProfile(2, Division.GOLD_1, 1170));

        rankingService.addPlayers(matchmakingProfiles);
        rankingService.rankPlayers();

        List<MatchmakingProfile> gold1Ranking = rankingService.getRanking(Division.GOLD_1);
        assertEquals(1, gold1Ranking.size());
        assertEquals(0, gold1Ranking.get(0).getId());

        List<MatchmakingProfile> silver3Ranking = rankingService.getRanking(Division.SILVER_2);
        assertEquals(2, silver3Ranking.size());
        assertEquals(1, silver3Ranking.get(0).getId());
        assertEquals(2, silver3Ranking.get(1).getId());
    }
}
