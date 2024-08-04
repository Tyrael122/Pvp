package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.pvp.interfaces.MatchService;
import org.example.pvp.interfaces.MatchmakingService;
import org.example.pvp.interfaces.RankingService;
import org.example.pvp.model.Match;
import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.model.Division;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@CrossOrigin
@RestController
public class PvpController {
    private final RankingService rankingService;
    private final MatchmakingService matchmakingService;
    private final MatchService matchService;

    private final List<MatchmakingProfile> matchmakingProfiles = new ArrayList<>();

    public PvpController(RankingService rankingService, MatchmakingService matchmakingService, MatchService matchService) {
        this.rankingService = rankingService;
        this.matchmakingService = matchmakingService;
        this.matchService = matchService;

        initializeThreads();
    }

    private void initializeThreads() {
        LocalDateTime nextMatchEndTime = matchService.calculateNextMatchEndTime();
        if (nextMatchEndTime != null) {
            ThreadManager.scheduleNewThreadToEndMatch(this::endMatches, nextMatchEndTime);
        }

        ThreadManager.scheduleNewThreadToStartMatchPeriodically(this::formMatches);
    }

    @PostMapping("auto-create-bulk-players")
    public void create10Players(@RequestParam int numPlayers) {
        for (int i = 0; i < numPlayers; i++) {
            turnOnPvpCreatingPlayer();
        }
    }

    // TODO: Demo purposes. Remove this endpoint in production.
    @PostMapping("auto-create-player")
    public long turnOnPvpCreatingPlayer() {
        long maxId = matchmakingProfiles.stream().mapToLong(MatchmakingProfile::getId).max().orElse(0);
        long newId = maxId + 1;

        matchmakingProfiles.add(new MatchmakingProfile(newId));

        turnOnPvp(newId);

        return newId;
    }

    @PostMapping("turn-on-pvp")
    public void turnOnPvp(@RequestParam long id) {
        log.info("Request to turn on PVP for player {}.", id);

        MatchmakingProfile matchmakingProfile = findById(id);
        if (matchmakingProfile == null) {
            matchmakingProfile = new MatchmakingProfile(id);

            matchmakingProfiles.add(matchmakingProfile);
        }

        matchmakingProfile.setAutoQueueOn(true);

        log.info("PVP turned on for player {}", id);

        matchmakingService.queuePlayers(List.of(matchmakingProfile));
        rankingService.addPlayers(List.of(matchmakingProfile));
    }

    @PostMapping("turn-off-pvp")
    public void turnOffPvp(@RequestParam long id) {
        log.info("Request to turn off PVP for player {}.", id);

        MatchmakingProfile matchmakingProfile = findById(id);
        if (matchmakingProfile == null) {
            log.info("Player {} not found.", id);

            return;
        }

        matchmakingProfile.setAutoQueueOn(false);

        log.info("Player {} turned off PVP.", id);

        matchmakingService.unqueuePlayers(List.of(matchmakingProfile));
    }

    @GetMapping("/me")
    public String getPlayer(@RequestParam long id) {
        MatchmakingProfile matchmakingProfile = findById(id);
        if (matchmakingProfile == null) {
            return "Player not found.";
        }

        return "Your id: " + matchmakingProfile.getId() + "\nYour rank: " + matchmakingProfile.getDivision() + "\nYour rating: " + matchmakingProfile.getRating() + "\nIs auto queuing on: " + matchmakingProfile.isAutoQueueOn();
    }

    @GetMapping("/match")
    public String getMatch(@RequestParam long id) {
        MatchmakingProfile matchmakingProfile = findById(id);

        Match match = matchService.getPlayerMatch(matchmakingProfile);
        if (match == null) {
            return "No match found for player " + id;
        }

        return match.toString();
    }

    @GetMapping("/rank")
    public String getRank() {
        log.info("Request to fetch ranking.");

        return "Current ranking:\n" + buildRankingList();
    }

    @PostMapping("/start-matches")
    public void formMatches() {
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

//        while (isMatchReady) {
//            List<Team> teams = matchmakingService.fetchTeamsForMatch();
//            matchService.startMatch(teams);
//
//            ThreadManager.scheduleNewThreadToEndMatch(this::endMatches, matchService.calculateNextMatchEndTime());
//
//            isMatchReady = matchmakingService.isMatchReady();
//        }
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

    private MatchmakingProfile findById(long id) {
        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            if (matchmakingProfile.getId() == id) {
                return matchmakingProfile;
            }
        }

        return null;
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
