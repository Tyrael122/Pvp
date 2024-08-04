package org.example.pvp.services;

import org.example.pvp.model.Division;
import org.example.pvp.model.MatchmakingProfile;

import java.util.*;

public class WeeklyRankingService {
    private final Map<Division, List<MatchmakingProfile>> ranking = new HashMap<>();
    private final Map<Division, Integer> moveUpCounts = createMoveUpCounts();
    private final Map<Division, Integer> moveDownCounts = createMoveDownCounts();

    public void addPlayers(List<MatchmakingProfile> matchmakingProfiles) {
        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            if (matchmakingProfile.getDivision() == null) {
                continue;
            }

            if (!ranking.containsKey(matchmakingProfile.getDivision())) {
                ranking.put(matchmakingProfile.getDivision(), new ArrayList<>());
            }

            ranking.get(matchmakingProfile.getDivision()).add(matchmakingProfile);
        }
    }

    public void removePlayers(List<MatchmakingProfile> matchmakingProfiles) {
        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            if (matchmakingProfile.getDivision() == null) {
                continue;
            }

            if (!ranking.containsKey(matchmakingProfile.getDivision())) {
                continue;
            }

            ranking.get(matchmakingProfile.getDivision()).remove(matchmakingProfile);
        }
    }

    public void rankPlayers() {
        for (Map.Entry<Division, List<MatchmakingProfile>> entry : ranking.entrySet()) {
            entry.getValue().sort((p1, p2) -> (int) (p2.getRating() - p1.getRating()));
        }

        Division[] divisions = Division.values();
        for (int i = 0; i < divisions.length - 1; i++) {
            if (divisions[i] == Division.UNRANKED) {
                continue;
            }

            movePlayers(divisions[i], divisions[i + 1]);
        }
    }

    public List<MatchmakingProfile> getRanking() {
        List<MatchmakingProfile> result = new ArrayList<>();

        for (Division division : Division.values()) {
            result.addAll(ranking.getOrDefault(division, List.of()));
        }

        return result;
    }

    public List<MatchmakingProfile> getRanking(Division division) {
        return ranking.getOrDefault(division, List.of());
    }

    private void movePlayers(Division lowerDivision, Division upperDivision) {
        List<MatchmakingProfile> upperRankMatchmakingProfiles = ranking.getOrDefault(upperDivision, List.of());
        List<MatchmakingProfile> lowerRankMatchmakingProfiles = ranking.getOrDefault(lowerDivision, List.of());

        int moveUpCount = moveUpCounts.get(upperDivision);
        int moveDownCount = moveDownCounts.get(lowerDivision);

        if (lowerRankMatchmakingProfiles.size() <= moveUpCount) {
            moveUpCount = 0;
        }

        if (upperRankMatchmakingProfiles.size() <= moveDownCount) {
            moveDownCount = 0;
        }

        List<MatchmakingProfile> playersGoingUp = lowerRankMatchmakingProfiles.subList(0, moveUpCount);
        List<MatchmakingProfile> playersGoingDown = upperRankMatchmakingProfiles.subList(upperRankMatchmakingProfiles.size() - moveDownCount, upperRankMatchmakingProfiles.size());

        upperRankMatchmakingProfiles = new ArrayList<>(upperRankMatchmakingProfiles.subList(0, upperRankMatchmakingProfiles.size() - moveDownCount));
        lowerRankMatchmakingProfiles = new ArrayList<>(lowerRankMatchmakingProfiles.subList(moveUpCount, lowerRankMatchmakingProfiles.size()));

        upperRankMatchmakingProfiles.addAll(playersGoingUp);
        lowerRankMatchmakingProfiles.addAll(0, playersGoingDown);

        changePlayerRanks(playersGoingUp, upperDivision);
        changePlayerRanks(playersGoingDown, lowerDivision);

        ranking.put(upperDivision, upperRankMatchmakingProfiles);
        ranking.put(lowerDivision, lowerRankMatchmakingProfiles);
    }

    private void changePlayerRanks(List<MatchmakingProfile> matchmakingProfiles, Division division) {
        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
//            player.setRank(rank);
        }
    }

    private Map<Division, Integer> createMoveUpCounts() {
        Map<Division, Integer> moveUpCounts = new HashMap<>();

        moveUpCounts.put(Division.BRONZE_1, 2);
        moveUpCounts.put(Division.BRONZE_2, 2);
        moveUpCounts.put(Division.SILVER_1, 2);
        moveUpCounts.put(Division.SILVER_2, 2);
        moveUpCounts.put(Division.GOLD_1, 2);
        moveUpCounts.put(Division.GOLD_2, 2);
        moveUpCounts.put(Division.PLATINUM_1, 2);
        moveUpCounts.put(Division.PLATINUM_2, 2);
        moveUpCounts.put(Division.MASTER, 2);
        moveUpCounts.put(Division.GRANDMASTER, 2);

        return moveUpCounts;
    }

    private Map<Division, Integer> createMoveDownCounts() {
        Map<Division, Integer> moveDownCounts = new HashMap<>();

        moveDownCounts.put(Division.BRONZE_1, 2);
        moveDownCounts.put(Division.BRONZE_2, 2);
        moveDownCounts.put(Division.SILVER_1, 2);
        moveDownCounts.put(Division.SILVER_2, 2);
        moveDownCounts.put(Division.GOLD_1, 2);
        moveDownCounts.put(Division.GOLD_2, 2);
        moveDownCounts.put(Division.PLATINUM_1, 2);
        moveDownCounts.put(Division.PLATINUM_2, 2);
        moveDownCounts.put(Division.MASTER, 2);
        moveDownCounts.put(Division.GRANDMASTER, 2);

        return moveDownCounts;
    }
}