package org.example.draftactual.matchmaking;

import org.example.draftactual.interfaces.MatchmakingService;
import org.example.draftactual.model.Player;
import org.example.draftactual.model.Range;
import org.example.draftactual.model.Team;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class VersusMatchmakingService implements MatchmakingService {
    private final List<WaitingTeam> waitingTeams = new ArrayList<>();
    private final List<WaitingTeam> completeWaitingTeams = new ArrayList<>();

    private final List<List<Team>> readyMatches = new ArrayList<>();

    private final int NUM_USERS_IN_TEAM;

    public VersusMatchmakingService() {
        this(3);
    }

    public VersusMatchmakingService(int numUsersInTeam) {
        NUM_USERS_IN_TEAM = numUsersInTeam;
    }

    @Override
    public void queuePlayer(Player player) {
        System.out.println("Request to queue player: " + player);

        WaitingPlayer waitingPlayer = new WaitingPlayer(player, LocalDateTime.now());

        WaitingTeam waitingTeam = new WaitingTeam();
        waitingTeam.addPlayer(waitingPlayer);

        if (waitingTeam.getWaitingPlayers().size() == NUM_USERS_IN_TEAM) {
            completeWaitingTeams.add(waitingTeam);
        } else {
            waitingTeams.add(waitingTeam);
        }
    }

    @Override
    public void unqueuePlayer(Player player) {
        waitingTeams.removeIf(team -> team.containsPlayer(player));
        completeWaitingTeams.removeIf(team -> team.containsPlayer(player));

        for (List<Team> match : readyMatches) {
            for (Team team : match) {
                if (team.getPlayers().contains(player)) {
                    readyMatches.remove(match);
                    break;
                }
            }
        }
    }

    @Override
    public boolean isMatchReady() {
        if (!readyMatches.isEmpty()) {
            return true;
        }

        tryToFormMatches();

        return !readyMatches.isEmpty();
    }

    @Override
    public List<Team> fetchTeamsForMatch() throws IllegalStateException {
        if (readyMatches.isEmpty()) {
            throw new IllegalStateException("No matches ready to be fetched.");
        }

        return readyMatches.removeFirst();
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
                    team1.addPlayer(team2.removePlayer(0));
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

        for (int i = 0; i < completeWaitingTeams.size() - 1; i++) {
            WaitingTeam team1 = completeWaitingTeams.get(i);
            WaitingTeam team2 = completeWaitingTeams.get(i + 1);

            if (canTeamsMatch(team1, team2)) {
                completeWaitingTeams.remove(i + 1);
                completeWaitingTeams.remove(i);
                i -= 2;

                readyTeams.add(List.of(team1.toTeam(), team2.toTeam()));
            }
        }

        return readyTeams;
    }

    private void sortByUpperRatingLimit(List<WaitingTeam> waitingTeams) {
        waitingTeams.sort(Comparator.comparing(team -> calculateRatingRange(team.getAverageRating(), team.calculateAverageWaitTime()).end()));
    }

    private boolean canTeamsMatch(WaitingTeam team1, WaitingTeam team2) {
        System.out.println("Can teams match: " + team1 + " vs " + team2);

        System.out.println("Team 1 range: " + calculateRatingRange(team1));
        System.out.println("Team 2 range: " + calculateRatingRange(team2));

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
}