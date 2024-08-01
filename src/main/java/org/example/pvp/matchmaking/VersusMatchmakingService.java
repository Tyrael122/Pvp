package org.example.pvp.matchmaking;

import lombok.extern.slf4j.Slf4j;
import org.example.pvp.stats.StatisticsService;
import org.example.pvp.interfaces.MatchmakingService;
import org.example.pvp.model.Player;
import org.example.pvp.model.Range;
import org.example.pvp.model.Team;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class VersusMatchmakingService implements MatchmakingService {
    private final List<WaitingTeam> waitingTeams = new ArrayList<>();
    private final List<WaitingTeam> completeWaitingTeams = new ArrayList<>();

    private final List<List<Team>> readyMatches = new ArrayList<>();

    private StatisticsService statisticsService;

    private final int NUM_USERS_IN_TEAM;

    public VersusMatchmakingService(StatisticsService statisticsService) {
        this(3);

        this.statisticsService = statisticsService;
    }

    public VersusMatchmakingService(int numUsersInTeam) {
        this.NUM_USERS_IN_TEAM = numUsersInTeam;
    }

    @Override
    public void queuePlayers(List<Player> players) {
        for (Player player : players) {
            queuePlayer(player);
        }
    }

    private void queuePlayer(Player player) {
        log.debug("Request to queue player: {}", player);

        WaitingPlayer waitingPlayer = new WaitingPlayer(player, LocalDateTime.now());

        WaitingTeam waitingTeam = new WaitingTeam();
        waitingTeam.getWaitingPlayers().add(waitingPlayer);

        if (waitingTeam.getWaitingPlayers().size() == NUM_USERS_IN_TEAM) {
            completeWaitingTeams.add(waitingTeam);
        } else {
            waitingTeams.add(waitingTeam);
        }
    }

    @Override
    public void unqueuePlayers(List<Player> players) {
        for (Player player : players) {
            unqueuePlayer(player);
        }
    }

    private void unqueuePlayer(Player player) {
        log.debug("Request to unqueue player: {}", player);

        boolean hasFoundPlayerAtReadyMatches = removeFromReadyMatches(player);
        if (hasFoundPlayerAtReadyMatches) {

            return;
        }

        boolean hasFoundPlayerAtCompleteWaitingTeams = removeFromCompleteWaitingTeams(player);
        if (hasFoundPlayerAtCompleteWaitingTeams) {
            return;
        }

        removeFromWaitingTeams(player);
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
    public List<Team> fetchTeamsForMatch() throws IllegalStateException {
        if (readyMatches.isEmpty()) {
            throw new IllegalStateException("No matches ready to be fetched.");
        }

        List<Team> teams = readyMatches.removeFirst();

        tryToDecreaseAverageRatingBetweenTeams(teams);

        statisticsService.addFormedMatch(teams);

        return teams;
    }

    private void tryToDecreaseAverageRatingBetweenTeams(List<Team> teams) {

    }

    private void tryToFormMatches() {
        tryToFormCompleteTeams();

        sortByUpperRatingLimit(completeWaitingTeams);

        List<List<Team>> compatibleTeams = findCompatibleTeams();
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

    private List<List<Team>> findCompatibleTeams() {
        if (completeWaitingTeams.size() < 2) {
            return List.of();
        }

        List<List<Team>> readyTeams = new ArrayList<>();

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

    private boolean removeFromReadyMatches(Player player) {
        for (int i = 0; i < readyMatches.size(); i++) {
            List<Team> teams = readyMatches.get(i);

            var teamWithPlayerToRemove = teams.stream().filter(team -> team.getPlayers().contains(player)).findFirst();

            if (teamWithPlayerToRemove.isPresent()) {
                teamWithPlayerToRemove.get().getPlayers().remove(player);

                for (Team team : teams) {
                    queuePlayers(team.getPlayers());
                }

                readyMatches.remove(i);

                return true;
            }
        }

        return false;
    }

    private boolean removeFromCompleteWaitingTeams(Player player) {
        for (int i = 0; i < completeWaitingTeams.size(); i++) {
            WaitingTeam team = completeWaitingTeams.get(i);

            if (team.getPlayers().contains(player)) {
                team.removePlayer(player);

                queuePlayers(team.getPlayers());

                completeWaitingTeams.remove(i);

                return true;
            }
        }

        return false;
    }

    private void removeFromWaitingTeams(Player player) {
        for (int i = 0; i < waitingTeams.size(); i++) {
            WaitingTeam team = waitingTeams.get(i);
            if (team.getPlayers().contains(player)) {
                team.removePlayer(player);

                waitingTeams.remove(i);

                return;
            }
        }
    }
}