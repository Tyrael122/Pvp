package org.example.pvp.services;

import org.example.pvp.interfaces.RankingService;
import org.example.pvp.model.Division;
import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.repositories.MatchmakingProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class VersusRankingServiceTest {

    @Autowired
    private MatchmakingProfileRepository matchmakingProfileRepository;

    private RankingService service;

    @BeforeEach
    void beforeAll() {
        service = new VersusRankingService(createRankLowerBoundaries(), matchmakingProfileRepository);
    }

    @Test
    void shouldAddPlayersToRank() {
        MatchmakingProfile matchmakingProfile = new MatchmakingProfile(1, 50);
        List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();
        matchmakingProfiles.add(matchmakingProfile);

        service.addPlayers(matchmakingProfiles);

        List<MatchmakingProfile> ranking = service.getRanking(Division.BRONZE_2);
        assertEquals(1, ranking.size());
        assertEquals(matchmakingProfile.getId(), ranking.getFirst().getId());
    }

    @Test
    void shouldAdd2PlayersAndRankThem() {
        MatchmakingProfile matchmakingProfile1 = new MatchmakingProfile(1, 50);
        MatchmakingProfile matchmakingProfile2 = new MatchmakingProfile(2, 100);

        List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();
        matchmakingProfiles.add(matchmakingProfile1);
        matchmakingProfiles.add(matchmakingProfile2);

        service.addPlayers(matchmakingProfiles);

        List<MatchmakingProfile> bronzeRanking = service.getRanking(Division.BRONZE_2);
        List<MatchmakingProfile> silverRanking = service.getRanking(Division.SILVER_1);

        assertEquals(1, bronzeRanking.size());
        assertEquals(matchmakingProfile1.getId(), bronzeRanking.getFirst().getId());

        assertEquals(1, silverRanking.size());
        assertEquals(matchmakingProfile2.getId(), silverRanking.getFirst().getId());
    }

    @Test
    void shouldAdd3PlayersAndRankThem() {
        MatchmakingProfile matchmakingProfile1 = new MatchmakingProfile(1, 50);
        MatchmakingProfile matchmakingProfile2 = new MatchmakingProfile(2, 100);
        MatchmakingProfile matchmakingProfile3 = new MatchmakingProfile(3, 200);

        List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();
        matchmakingProfiles.add(matchmakingProfile1);
        matchmakingProfiles.add(matchmakingProfile2);
        matchmakingProfiles.add(matchmakingProfile3);

        service.addPlayers(matchmakingProfiles);

        List<MatchmakingProfile> bronzeRanking = service.getRanking(Division.BRONZE_2);
        List<MatchmakingProfile> silverRanking = service.getRanking(Division.SILVER_1);
        List<MatchmakingProfile> goldRanking = service.getRanking(Division.GOLD_1);

        assertEquals(1, bronzeRanking.size());
        assertEquals(matchmakingProfile1.getId(), bronzeRanking.getFirst().getId());

        assertEquals(1, silverRanking.size());
        assertEquals(matchmakingProfile2.getId(), silverRanking.getFirst().getId());

        assertEquals(1, goldRanking.size());
        assertEquals(matchmakingProfile3.getId(), goldRanking.getFirst().getId());
    }

    @Test
    void shouldRemoveMatchmakingProfileFromRank() {
        MatchmakingProfile matchmakingProfile = new MatchmakingProfile(1, 50);

        List<MatchmakingProfile> matchmakingProfiles = List.of(matchmakingProfile);

        service.addPlayers(matchmakingProfiles);
        service.removePlayers(matchmakingProfiles);

        List<MatchmakingProfile> ranking = service.getRanking(Division.BRONZE_2);
        assertEquals(0, ranking.size());
    }

    @Test
    void shouldRemoveMatchmakingProfileFromRankAndRankThem() {
        MatchmakingProfile matchmakingProfile1 = new MatchmakingProfile(1, 50);
        MatchmakingProfile matchmakingProfile2 = new MatchmakingProfile(2, 60);

        List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();
        matchmakingProfiles.add(matchmakingProfile1);
        matchmakingProfiles.add(matchmakingProfile2);

        service.addPlayers(matchmakingProfiles);

        List<MatchmakingProfile> ranking = service.getRanking(Division.BRONZE_2);

        assertEquals(2, ranking.size());
        assertEquals(matchmakingProfile2.getId(), ranking.getFirst().getId());

        service.removePlayers(List.of(matchmakingProfile2));

        ranking = service.getRanking(Division.BRONZE_2);
        assertEquals(1, ranking.size());
        assertEquals(matchmakingProfile1.getId(), ranking.getFirst().getId());
    }

    @Test
    void shouldUpdate1PlayerRanking() {
        MatchmakingProfile matchmakingProfile = new MatchmakingProfile(1, 260);
        List<MatchmakingProfile> matchmakingProfiles = List.of(matchmakingProfile);

        service.addPlayers(matchmakingProfiles);

        matchmakingProfile.setRating(240); // Update the rating
        service.updateRankings(matchmakingProfiles);

        List<MatchmakingProfile> ranking = service.getRanking(Division.GOLD_1);
        assertEquals(1, ranking.size());
        assertEquals(matchmakingProfile.getId(), ranking.getFirst().getId());
    }

    @Test
    void shouldUpdate2PlayersRanking() {
        MatchmakingProfile matchmakingProfile1 = new MatchmakingProfile(1, 50);
        MatchmakingProfile matchmakingProfile2 = new MatchmakingProfile(2, 60);

        List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();
        matchmakingProfiles.add(matchmakingProfile1);
        matchmakingProfiles.add(matchmakingProfile2);

        service.addPlayers(matchmakingProfiles);

        matchmakingProfile1.setRating(75); // Update the rating
        matchmakingProfile2.setRating(70); // Update the rating

        service.updateRankings(matchmakingProfiles);

        List<MatchmakingProfile> ranking = service.getRanking(Division.BRONZE_2);

        assertEquals(2, ranking.size());
        assertEquals(matchmakingProfile1.getId(), ranking.getFirst().getId());
        assertEquals(matchmakingProfile2.getId(), ranking.getLast().getId());
    }

    @Test
    void shouldGetAllRanking() {
        MatchmakingProfile matchmakingProfile1 = new MatchmakingProfile(1, 50);
        MatchmakingProfile matchmakingProfile2 = new MatchmakingProfile(2, 60);
        MatchmakingProfile matchmakingProfile3 = new MatchmakingProfile(3, 70);

        List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();
        matchmakingProfiles.add(matchmakingProfile1);
        matchmakingProfiles.add(matchmakingProfile2);
        matchmakingProfiles.add(matchmakingProfile3);

        service.addPlayers(matchmakingProfiles);

        List<MatchmakingProfile> ranking = service.getRanking();

        assertEquals(3, ranking.size());
        assertEquals(matchmakingProfile3.getId(), ranking.get(0).getId());
        assertEquals(matchmakingProfile2.getId(), ranking.get(1).getId());
        assertEquals(matchmakingProfile1.getId(), ranking.get(2).getId());
    }

    @Test
    void shouldSaveRankingInRepository() {
        MatchmakingProfile matchmakingProfile1 = new MatchmakingProfile(1, 50);
        MatchmakingProfile matchmakingProfile2 = new MatchmakingProfile(2, 60);
        MatchmakingProfile matchmakingProfile3 = new MatchmakingProfile(3, 70);

        List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();
        matchmakingProfiles.add(matchmakingProfile1);
        matchmakingProfiles.add(matchmakingProfile2);
        matchmakingProfiles.add(matchmakingProfile3);

        service.addPlayers(matchmakingProfiles);

        service = new VersusRankingService(createRankLowerBoundaries(), matchmakingProfileRepository);

        List<MatchmakingProfile> ranking = service.getRanking();

        assertEquals(3, ranking.size());
        assertEquals(matchmakingProfile3.getId(), ranking.get(0).getId());
        assertEquals(matchmakingProfile2.getId(), ranking.get(1).getId());
        assertEquals(matchmakingProfile1.getId(), ranking.get(2).getId());
    }

    private static Map<Integer, Division> createRankLowerBoundaries() {
        Map<Integer, Division> rankBoundaries = new HashMap<>();

        rankBoundaries.put(0, Division.BRONZE_1);
        rankBoundaries.put(50, Division.BRONZE_2);
        rankBoundaries.put(100, Division.SILVER_1);
        rankBoundaries.put(150, Division.SILVER_2);
        rankBoundaries.put(200, Division.GOLD_1);
        rankBoundaries.put(250, Division.GOLD_2);
        rankBoundaries.put(300, Division.PLATINUM_1);
        rankBoundaries.put(350, Division.PLATINUM_2);
        rankBoundaries.put(400, Division.MASTER);

        return rankBoundaries;
    }
}