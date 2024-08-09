package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.pvp.interfaces.MatchService;
import org.example.pvp.interfaces.MatchmakingService;
import org.example.pvp.interfaces.RankingService;
import org.example.pvp.model.*;
import org.example.pvp.repositories.MatchmakingProfileRepository;
import org.example.pvp.repositories.PlayerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@CrossOrigin
@RestController
public class VersusPvpController {
    private final String ENDPOINT_PREFIX = "/pvp";

    private final RankingService rankingService;
    private final MatchmakingService matchmakingService;
    private final MatchService matchService;

    private final PlayerRepository playerRepository;
    private final MatchmakingProfileRepository matchmakingProfileRepository;

//    private final List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();

    public VersusPvpController(RankingService rankingService, MatchmakingService matchmakingService, MatchService matchService, PlayerRepository playerRepository, MatchmakingProfileRepository matchmakingProfileRepository) {
        this.rankingService = rankingService;
        this.matchmakingService = matchmakingService;
        this.matchService = matchService;
        this.playerRepository = playerRepository;
        this.matchmakingProfileRepository = matchmakingProfileRepository;

        initializeThreads();
    }

    private void initializeThreads() {
        LocalDateTime nextMatchEndTime = matchService.calculateNextMatchEndTime();
        if (nextMatchEndTime != null) {
            ThreadManager.scheduleNewThreadToEndMatch(this::endMatches, nextMatchEndTime);
        }

        ThreadManager.scheduleNewThreadToStartMatchPeriodically(this::formMatches);
    }

    @PostMapping(ENDPOINT_PREFIX + "/{playerId}")
    public void createPlayerProfile(@PathVariable long playerId) {
        log.info("Request to turn on PVP for player {}.", playerId);

        Player player = playerRepository.findById(playerId).orElseThrow();
        MatchmakingProfile matchmakingProfile = player.findMatchmakingProfileByRankingMode(RankingMode.VERSUS);
        if (matchmakingProfile == null) {
            matchmakingProfile = new MatchmakingProfile(RankingMode.VERSUS);

            player.getProfiles().add(matchmakingProfile);

            playerRepository.save(player);
        }

        matchmakingProfile.setAutoQueueOn(true);

        log.info("PVP turned on for player {}", playerId);

        matchmakingService.queuePlayers(List.of(matchmakingProfile));
        rankingService.addPlayers(List.of(matchmakingProfile));

        matchmakingProfileRepository.save(matchmakingProfile);
    }

    @PostMapping(ENDPOINT_PREFIX + "/{playerId}/toggle")
    public void togglePvp(@PathVariable long playerId) {
        log.info("Request to toggle PVP for player {}.", playerId);

        MatchmakingProfile matchmakingProfile = findMatchmakingProfileByPlayerId(playerId);

        if (matchmakingProfile.isAutoQueueOn()) {
            log.info("Turning off PVP for player {}.", playerId);

            matchmakingProfile.setAutoQueueOn(false);
            matchmakingService.unqueuePlayers(List.of(matchmakingProfile));
        } else {
            log.info("Turning on PVP for player {}.", playerId);

            matchmakingProfile.setAutoQueueOn(true);
            matchmakingService.queuePlayers(List.of(matchmakingProfile));
        }

        matchmakingProfileRepository.save(matchmakingProfile);
    }

    @GetMapping(ENDPOINT_PREFIX + "/{playerId}/match")
    public Match getMatch(@PathVariable long playerId) {
        MatchmakingProfile matchmakingProfile = findMatchmakingProfileByPlayerId(playerId);

        return matchService.getPlayerMatch(matchmakingProfile);
    }

    private MatchmakingProfile findMatchmakingProfileByPlayerId(long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow();

        return player.findMatchmakingProfileByRankingMode(RankingMode.VERSUS);
    }

    @GetMapping("/rank")
    public String getRank() {
        log.info("Request to fetch ranking.");

        return "Current ranking:\n" + buildRankingList();
    }

    private void formMatches() {
        boolean isMatchReady = matchmakingService.isMatchReady();
        if (!isMatchReady) {
            return;
        }

        List<List<MatchGroup>> matches = new ArrayList<>();
        while (isMatchReady) {
            List<MatchGroup> matchGroups = matchmakingService.fetchTeamsForMatch();
            matches.add(matchGroups);

            isMatchReady = matchmakingService.isMatchReady();
        }

        for (List<MatchGroup> matchGroups : matches) {
            matchService.startMatch(matchGroups);
        }

        ThreadManager.scheduleNewThreadToEndMatch(this::endMatches, matchService.calculateNextMatchEndTime());
    }

    private void endMatches() {
        List<Match> endedMatches = matchService.endMatchesReadyToEnd();

        for (Match matches : endedMatches) {
            for (MatchGroup matchGroup : matches.getMatchGroups()) {
                for (MatchmakingProfile matchmakingProfile : matchGroup.getMatchmakingProfiles()) {
                    if (matchmakingProfile.isAutoQueueOn()) {
                        matchmakingService.queuePlayers(List.of(matchmakingProfile));

                        log.info("Player {} auto queued.", matchmakingProfile.getId());
                    }
                }
            }
        }
    }

    private String buildRankingList() {
        StringBuilder rankingList = new StringBuilder();

        List<Division> divisions = Arrays.asList(Division.values()).reversed();
        for (Division division : divisions) {
            rankingList.append(division).append(":\n");

            for (MatchmakingProfile matchmakingProfile : rankingService.getRanking(division)) {
                rankingList.append(matchmakingProfile.getId()).append(": ").append(matchmakingProfile.getRating()).append("\n");
            }
        }

        return rankingList.toString();
    }
}
