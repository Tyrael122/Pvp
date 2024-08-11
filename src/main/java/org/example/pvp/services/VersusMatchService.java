package org.example.pvp.services;

import lombok.extern.slf4j.Slf4j;
import org.example.pvp.interfaces.EloRatingService;
import org.example.pvp.interfaces.MatchService;
import org.example.pvp.interfaces.RankingService;
import org.example.pvp.interfaces.WinnerCalculator;
import org.example.pvp.model.Match;
import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchStatus;
import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.repositories.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Service
public class VersusMatchService implements MatchService {
    private final List<Match> currentMatches = new ArrayList<>();

    private final WinnerCalculator winnerCalculator;
    private final EloRatingService eloRatingService;
    private final RankingService rankingService;

    private final MatchRepository matchRepository;

    private final Supplier<LocalDateTime> matchEndSupplier;

    public VersusMatchService(WinnerCalculator winnerCalculator, EloRatingService eloRatingService, RankingService rankingService, MatchRepository matchRepository, Supplier<LocalDateTime> matchEndSupplier) {
        this.winnerCalculator = winnerCalculator;
        this.eloRatingService = eloRatingService;
        this.rankingService = rankingService;
        this.matchRepository = matchRepository;

        this.matchEndSupplier = matchEndSupplier;

        initializeCurrentMatches();
    }

    @Autowired
    public VersusMatchService(WinnerCalculator winnerCalculator, EloRatingService eloRatingService, RankingService rankingService, MatchRepository matchRepository) {
        this(winnerCalculator, eloRatingService, rankingService, matchRepository, () -> LocalDateTime.now().plusHours(24));
    }

    @Override
    public void startMatch(List<MatchGroup> matchGroups) {
        Match match = new Match(matchGroups);
        match.start(matchEndSupplier.get());

        addMatch(match);

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

            endMatch(match, winner);

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

    private void initializeCurrentMatches() {
        List<Match> matches = matchRepository.findAllByMatchStatus(MatchStatus.IN_PROGRESS);
        currentMatches.addAll(matches);
    }

    private void addMatch(Match match) {
        currentMatches.add(match);
        matchRepository.save(match);
    }

    private void endMatch(Match match, MatchGroup winner) {
        match.end(winner);
        matchRepository.save(match);
    }

    private void updateRankings(Match match) {
        for (MatchGroup matchGroup : match.getMatchGroups()) {
            rankingService.updateRankings(matchGroup.getMatchmakingProfiles());
        }
    }
}
