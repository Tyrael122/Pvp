package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.pvp.interfaces.MatchService;
import org.example.pvp.interfaces.MatchmakingService;
import org.example.pvp.interfaces.RankingService;
import org.example.pvp.model.Match;
import org.example.pvp.model.Player;
import org.example.pvp.model.Rank;
import org.example.pvp.model.Team;
import org.example.pvp.stats.RankingStatistics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin
@RestController
@Slf4j
public class PvpController {
    private final RankingService rankingService;
    private final MatchmakingService matchmakingService;
    private final MatchService matchService;

    private final List<Player> players = new ArrayList<>();

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
        long maxId = players.stream().mapToLong(Player::getId).max().orElse(0);
        long newId = maxId + 1;

        players.add(new Player(newId));

        turnOnPvp(newId);

        return newId;
    }

    @PostMapping("turn-on-pvp")
    public void turnOnPvp(@RequestParam long id) {
        log.info("Request to turn on PVP for player {}.", id);

        Player player = findById(id);
        if (player == null) {
            player = new Player(id);

            players.add(player);
        }

        player.setAutoQueueOn(true);

        log.info("PVP turned on for player {}", id);

        matchmakingService.queuePlayers(List.of(player));
        rankingService.addPlayers(List.of(player));
    }

    @PostMapping("turn-off-pvp")
    public void turnOffPvp(@RequestParam long id) {
        log.info("Request to turn off PVP for player {}.", id);

        Player player = findById(id);
        if (player == null) {
            log.info("Player {} not found.", id);

            return;
        }

        player.setAutoQueueOn(false);

        log.info("Player {} turned off PVP.", id);

        matchmakingService.unqueuePlayers(List.of(player));
    }

    @GetMapping("/me")
    public String getPlayer(@RequestParam long id) {
        Player player = findById(id);
        if (player == null) {
            return "Player not found.";
        }

        return "Your id: " + player.getId() + "\nYour rank: " + player.getRank() + "\nYour rating: " + player.getRating() + "\nIs auto queuing on: " + player.isAutoQueueOn();
    }

    @GetMapping("/match")
    public String getMatch(@RequestParam long id) {
        Player player = findById(id);

        Match match = matchService.getPlayerMatch(player);
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

        List<List<Team>> matches = new ArrayList<>();
        while (isMatchReady) {
            List<Team> teams = matchmakingService.fetchTeamsForMatch();
            matches.add(teams);

            isMatchReady = matchmakingService.isMatchReady();
        }

        for (List<Team> teams : matches) {
            matchService.startMatch(teams);
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
            for (Team team : matches.getTeams()) {
                for (Player player : team.getPlayers()) {
                    if (player.isAutoQueueOn()) {
                        matchmakingService.queuePlayers(List.of(player));

                        log.info("Player {} auto queued.", player.getId());
                    }
                }
            }
        }
    }

    private Player findById(long id) {
        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }

        return null;
    }

    private String buildRankingList() {
        StringBuilder rankingList = new StringBuilder();

        List<Rank> ranks = Arrays.asList(Rank.values()).reversed();
        for (Rank rank : ranks) {
            rankingList.append(rank).append(":\n");

            for (Player player : rankingService.getRanking(rank)) {
                rankingList.append(player.getId()).append(": ").append(player.getRating()).append("\n");
            }
        }

        return rankingList.toString();
    }
}
