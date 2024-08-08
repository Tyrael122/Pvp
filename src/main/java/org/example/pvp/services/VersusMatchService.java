package org.example.pvp.services;

import lombok.extern.slf4j.Slf4j;
import org.example.pvp.interfaces.EloRatingService;
import org.example.pvp.interfaces.MatchService;
import org.example.pvp.interfaces.RankingService;
import org.example.pvp.interfaces.WinnerCalculator;
import org.example.pvp.model.Match;
import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchmakingProfile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
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
    public void startMatch(List<MatchGroup> matchGroups) {
        Match match = new Match(matchGroups);
//        match.start(LocalDateTime.now().plusHours(24));
        match.start(LocalDateTime.now().plusSeconds(1));

        currentMatches.add(match);

        log.debug("Match started: {}", match);
    }

    @Override
    public Match getPlayerMatch(MatchmakingProfile matchmakingProfile) {
        for (Match match : currentMatches) {
            for (MatchGroup matchGroup : match.getMatchGroups()) {
                for (MatchmakingProfile teamMatchmakingProfile : matchGroup.getMatchmakingProfiles()) {
                    if (teamMatchmakingProfile.equals(matchmakingProfile)) {
                        return match;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public List<Match> endMatchesReadyToEnd() {
        log.debug("Checking form matches ready to end.");

        List<Match> endedMatches = new ArrayList<>();

        Match match = currentMatches.getFirst();
        while (LocalDateTime.now().isAfter(match.getScheduledEndTime())) {
            MatchGroup winner = winnerCalculator.calculateWinner(match.getMatchGroups());

            eloRatingService.updateRatings(match.getMatchGroups(), winner);
            updateRankings(match);

            match.end(winner);

            log.debug("Match ended: {}", match);

            endedMatches.add(currentMatches.removeFirst());

            if (currentMatches.isEmpty()) {
                break;
            }

            match = currentMatches.getFirst();
        }

        return endedMatches;
    }

    @Override
    public LocalDateTime calculateNextMatchEndTime() {
        if (currentMatches.isEmpty()) {
            return null;
        }

        return currentMatches.getFirst().getScheduledEndTime();
    }

    private void updateRankings(Match match) {
        for (MatchGroup matchGroup : match.getMatchGroups()) {
            rankingService.updateRankings(matchGroup.getMatchmakingProfiles());
        }
    }
}
