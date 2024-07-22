package org.example.draftactual.services;

import org.example.draftactual.interfaces.EloRatingService;
import org.example.draftactual.interfaces.MatchService;
import org.example.draftactual.interfaces.RankingService;
import org.example.draftactual.interfaces.WinnerCalculator;
import org.example.draftactual.model.Match;
import org.example.draftactual.model.Player;
import org.example.draftactual.model.Team;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VersusMatchService implements MatchService {
    private final List<Match> currentMatches = new ArrayList<>();

    private final WinnerCalculator winnerCalculator;
    private final EloRatingService eloRatingService;
    private final RankingService rankingService;

    public VersusMatchService(WinnerCalculator winnerCalculator, EloRatingService eloRatingService, RankingService rankingService) {
        this.winnerCalculator = winnerCalculator;
        this.eloRatingService = eloRatingService;
        this.rankingService = rankingService;
    }

    @Override
    public void startMatch(List<Team> teams) {
        Match match = new Match(teams);
//        match.start(LocalDateTime.now().plusHours(24));
        match.start(LocalDateTime.now().plusSeconds(5));

        currentMatches.add(match);
    }

    @Override
    public Match getPlayerMatch(Player player) {
        for (Match match : currentMatches) {
            for (Team team : match.getTeams()) {
                for (Player teamPlayer : team.getPlayers()) {
                    if (teamPlayer.equals(player)) {
                        return match;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public List<Match> endMatchesReadyToEnd() {
        List<Match> endedMatches = new ArrayList<>();

        Match match = currentMatches.getFirst();
        while (LocalDateTime.now().isAfter(match.getScheduledEndTime())) {
            Team winner = winnerCalculator.calculateWinner(match.getTeams());

            eloRatingService.updateRatings(match.getTeams(), winner);
            updateRankings(match);

            match.end(winner);
            System.out.println("Match ended: " + match);

            endedMatches.add(currentMatches.removeFirst());

            if (currentMatches.isEmpty()) {
                break;
            }

            match = currentMatches.getFirst();
        }

        System.out.println("Ended matches: " + endedMatches.size());

        return endedMatches;
    }

    @Override
    public LocalDateTime calculateNextMatchEndTime() {
        return currentMatches.getFirst().getScheduledEndTime();
    }

    private void updateRankings(Match match) {
        for (Team team : match.getTeams()) {
            rankingService.updateRankings(team.getPlayers());
        }
    }
}