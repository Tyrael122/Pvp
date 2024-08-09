package org.example.pvp.matchmaking;

import lombok.extern.slf4j.Slf4j;
import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.stats.StatisticsService;
import org.example.pvp.interfaces.MatchmakingService;
import org.example.pvp.model.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class VersusMatchmakingService implements MatchmakingService {
    private final List<WaitingTeam> waitingTeams = new ArrayList<>();
    private final List<WaitingTeam> completeWaitingTeams = new ArrayList<>();

    private final List<List<MatchGroup>> readyMatches = new ArrayList<>();

    private StatisticsService statisticsService;

    private final int NUM_USERS_IN_TEAM;

    private final WaitingTeamRepository waitingTeamRepository;

    @Autowired
    public VersusMatchmakingService(StatisticsService statisticsService, WaitingTeamRepository waitingTeamRepository) {
        this(3, waitingTeamRepository);

        this.statisticsService = statisticsService;
    }

    public VersusMatchmakingService(int numUsersInTeam, WaitingTeamRepository waitingTeamRepository) {
        this.NUM_USERS_IN_TEAM = numUsersInTeam;
        this.waitingTeamRepository = waitingTeamRepository;

        fetchWaitingTeamsFromRepository();
    }

    @Override
    public void queuePlayers(List<MatchmakingProfile> matchmakingProfiles) {
        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            queuePlayer(matchmakingProfile);
        }
    }

    private void queuePlayer(MatchmakingProfile matchmakingProfile) {
        log.debug("Request to queue player: {}", matchmakingProfile);

        WaitingPlayer waitingPlayer = new WaitingPlayer(matchmakingProfile, LocalDateTime.now());

        WaitingTeam waitingTeam = new WaitingTeam();
        waitingTeam.getWaitingPlayers().add(waitingPlayer);

        if (waitingTeam.getWaitingPlayers().size() == NUM_USERS_IN_TEAM) {
            completeWaitingTeams.add(waitingTeam);
        } else {
            waitingTeams.add(waitingTeam);
        }

        waitingTeamRepository.save(waitingTeam);
    }

    @Override
    public void unqueuePlayers(List<MatchmakingProfile> matchmakingProfiles) {
        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            unqueuePlayer(matchmakingProfile);
        }
    }

    private void unqueuePlayer(MatchmakingProfile matchmakingProfile) {
        log.debug("Request to unqueue player: {}", matchmakingProfile);

        waitingTeamRepository.deleteById(matchmakingProfile.getId());

        boolean hasFoundPlayerAtReadyMatches = removeFromReadyMatches(matchmakingProfile);
        if (hasFoundPlayerAtReadyMatches) {

            return;
        }

        boolean hasFoundPlayerAtCompleteWaitingTeams = removeFromCompleteWaitingTeams(matchmakingProfile);
        if (hasFoundPlayerAtCompleteWaitingTeams) {
            return;
        }

        removeFromWaitingTeams(matchmakingProfile);
    }

    @Override
    public boolean isMatchReady() {
        log.debug("Checking if match is ready.");

        if (!readyMatches.isEmpty()) {
            return true;
        }

        tryToFormMatches();

        boolean isMatchReady = !readyMatches.isEmpty();

        log.debug("Is match ready: {}", isMatchReady);

        return isMatchReady;
    }

    @Override
    public List<MatchGroup> fetchTeamsForMatch() throws IllegalStateException {
        if (readyMatches.isEmpty()) {
            throw new IllegalStateException("No matches ready to be fetched.");
        }

        List<MatchGroup> matchGroups = readyMatches.removeFirst();

        tryToDecreaseAverageRatingBetweenTeams(matchGroups);

        removeFromRepository(matchGroups);

        statisticsService.addFormedMatch(matchGroups);

        return matchGroups;
    }

    private void tryToDecreaseAverageRatingBetweenTeams(List<MatchGroup> matchGroups) {

    }

    private void tryToFormMatches() {
        tryToFormCompleteTeams();

        sortByUpperRatingLimit(completeWaitingTeams);

        List<List<MatchGroup>> compatibleTeams = findCompatibleTeams();
        readyMatches.addAll(compatibleTeams);
    }

    private void tryToFormCompleteTeams() {
        sortByUpperRatingLimit(waitingTeams);

        for (int i = waitingTeams.size() - 1; i > 0; i--) {
            WaitingTeam team1 = waitingTeams.get(i - 1);
            WaitingTeam team2 = waitingTeams.get(i);

            if (canTeamsMatch(team1, team2)) {
                while (team1.getWaitingPlayers().size() < NUM_USERS_IN_TEAM && !team2.getWaitingPlayers().isEmpty()) {
                    team1.getWaitingPlayers().add(team2.removePlayer(0));
                }

                if (team1.getWaitingPlayers().size() == NUM_USERS_IN_TEAM) {
                    completeWaitingTeams.add(team1);
                    waitingTeams.remove(i - 1);
                    i--;
                }

                if (team2.getWaitingPlayers().isEmpty()) {
                    waitingTeams.remove(i);
                }
            }
        }
    }

    private List<List<MatchGroup>> findCompatibleTeams() {
        if (completeWaitingTeams.size() < 2) {
            return List.of();
        }

        List<List<MatchGroup>> readyTeams = new ArrayList<>();

        for (int i = completeWaitingTeams.size() - 1; i > 0; i--) {
            WaitingTeam team1 = completeWaitingTeams.get(i);
            WaitingTeam team2 = completeWaitingTeams.get(i - 1);

            if (canTeamsMatch(team1, team2)) {
                completeWaitingTeams.remove(i);
                completeWaitingTeams.remove(i - 1);
                i--;

                readyTeams.add(List.of(team1.toTeam(), team2.toTeam()));
            }
        }

        return readyTeams;
    }

    private void sortByUpperRatingLimit(List<WaitingTeam> waitingTeams) {
        waitingTeams.sort(Comparator.comparing(team -> calculateRatingRange(team.getAverageRating(), team.calculateAverageWaitTime()).end()));
    }

    private boolean canTeamsMatch(WaitingTeam team1, WaitingTeam team2) {
        team1.bufferAverageRating();
        team2.bufferAverageRating();

        return calculateRatingRange(team1).overlap(calculateRatingRange(team2));
    }

    private Range calculateRatingRange(WaitingTeam team) {
        return calculateRatingRange(team.getAverageRating(), team.calculateAverageWaitTime());
    }

    private Range calculateRatingRange(double rating, Duration waitTime) {
        long deviation = (waitTime.getSeconds() / 30) * 50;

        int fixedDeviation = 100;

        double lower = rating - fixedDeviation - deviation;
        double upper = rating + fixedDeviation + deviation;

        return new Range(lower, upper);
    }

    private boolean removeFromReadyMatches(MatchmakingProfile matchmakingProfile) {
        for (int i = 0; i < readyMatches.size(); i++) {
            List<MatchGroup> matchGroups = readyMatches.get(i);

            var teamWithPlayerToRemove = matchGroups.stream().filter(team -> team.getMatchmakingProfiles().contains(matchmakingProfile)).findFirst();

            if (teamWithPlayerToRemove.isPresent()) {
                teamWithPlayerToRemove.get().getMatchmakingProfiles().remove(matchmakingProfile);

                for (MatchGroup matchGroup : matchGroups) {
                    queuePlayers(matchGroup.getMatchmakingProfiles());
                }

                readyMatches.remove(i);

                return true;
            }
        }

        return false;
    }

    private boolean removeFromCompleteWaitingTeams(MatchmakingProfile matchmakingProfile) {
        for (int i = 0; i < completeWaitingTeams.size(); i++) {
            WaitingTeam team = completeWaitingTeams.get(i);

            if (team.getPlayers().contains(matchmakingProfile)) {
                team.removePlayer(matchmakingProfile);

                queuePlayers(team.getPlayers());

                completeWaitingTeams.remove(i);

                return true;
            }
        }

        return false;
    }

    private void removeFromWaitingTeams(MatchmakingProfile matchmakingProfile) {
        for (int i = 0; i < waitingTeams.size(); i++) {
            WaitingTeam team = waitingTeams.get(i);
            if (team.getPlayers().contains(matchmakingProfile)) {
                team.removePlayer(matchmakingProfile);

                waitingTeams.remove(i);

                return;
            }
        }
    }

    private void fetchWaitingTeamsFromRepository() {
        List<WaitingTeam> storedWaitingTeams = waitingTeamRepository.findAll();
        waitingTeams.addAll(storedWaitingTeams);
    }

    private void removeFromRepository(List<MatchGroup> matchGroups) {
        for (MatchGroup matchGroup : matchGroups) {
            for (MatchmakingProfile matchmakingProfile : matchGroup.getMatchmakingProfiles()) {
                waitingTeamRepository.deleteById(matchmakingProfile.getId());
            }
        }
    }
}