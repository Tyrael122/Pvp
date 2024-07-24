package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.AskForWinnerCalculator;
import org.example.draftactual.interfaces.MatchService;
import org.example.draftactual.interfaces.MatchmakingService;
import org.example.draftactual.interfaces.RankingService;
import org.example.draftactual.model.Match;
import org.example.draftactual.model.Player;
import org.example.draftactual.model.Rank;
import org.example.draftactual.model.Team;
import org.example.draftactual.services.VersusEloRatingService;
import org.example.draftactual.services.VersusMatchService;
import org.example.draftactual.matchmaking.VersusMatchmakingService;
import org.example.draftactual.services.VersusRankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CrossOrigin
@RestController
@Slf4j
public class PvpController {
    private static final RankingService rankingService = new VersusRankingService();

    private static final MatchmakingService matchmakingService = new VersusMatchmakingService();
    private static final MatchService matchService = new VersusMatchService(new AskForWinnerCalculator(), new VersusEloRatingService(), rankingService);

    private final List<Player> players = new ArrayList<>();

    public PvpController() {
        LocalDateTime nextMatchEndTime = matchService.calculateNextMatchEndTime();
        if (nextMatchEndTime != null) {
            ThreadManager.scheduleNewThreadToEndMatch(PvpController::endMatches, nextMatchEndTime);
        }

        ThreadManager.scheduleNewThreadToStartMatchPeriodically(this::formMatches);
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

        while (isMatchReady) {
            List<Team> teams = matchmakingService.fetchTeamsForMatch();
            matchService.startMatch(teams);

            ThreadManager.scheduleNewThreadToEndMatch(PvpController::endMatches, matchService.calculateNextMatchEndTime());

            isMatchReady = matchmakingService.isMatchReady();
        }
    }

    private static void endMatches() {
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
