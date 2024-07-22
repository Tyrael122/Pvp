package org.example.controller;

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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@RestController
public class PvpController {
    private static final RankingService rankingService = new VersusRankingService();

    private static final MatchmakingService matchmakingService = new VersusMatchmakingService();
    private static final MatchService matchService = new VersusMatchService(new AskForWinnerCalculator(), new VersusEloRatingService(), rankingService);

    //    private final List<Player> players = createSamplePlayers();
    private final List<Player> players = new ArrayList<>();

    @PostMapping("turn-on-pvp")
    public void turnOnPvp(@RequestParam long id) {
        Player player = findById(id);
        if (player == null) {
            player = new Player(id);

            players.add(player);
        }

//        player.setAutoQueueOn(true);

        System.out.println("Player " + id + " turned on PVP.");

        matchmakingService.queuePlayer(player);
        rankingService.addPlayers(List.of(player));
    }

    @PostMapping("turn-off-pvp")
    public void turnOffPvp(@RequestParam long id) {
        Player player = findById(id);
        if (player == null) {
            System.out.println("Player " + id + " not found.");

            return;
        }

//        player.setAutoQueueOn(false);

        System.out.println("Player " + id + " turned off PVP.");

        matchmakingService.unqueuePlayer(player);
    }

    @GetMapping("/me")
    public String getPlayer(@RequestParam long id) {
        Player player = findById(id);
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
        return "Current ranking:\n" + buildRankingList();
    }

    @PostMapping("/start-matches")
    public void formMatches() {
        boolean isMatchReady = matchmakingService.isMatchReady();
        System.out.println("Is match ready: " + isMatchReady);

        while (isMatchReady) {
            List<Team> teams = matchmakingService.fetchTeamsForMatch();
            matchService.startMatch(teams);

            System.out.println("Match started: " + teams.get(0).getPlayers() + " vs " + teams.get(1).getPlayers());

            scheduleNewThreadToEndMatch();

            isMatchReady = matchmakingService.isMatchReady();
        }
    }

    private void scheduleNewThreadToEndMatch() {
        LocalDateTime nextMatchEndTime = matchService.calculateNextMatchEndTime();
        try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
            scheduler.schedule(PvpController::endMatches, getDelay(nextMatchEndTime), TimeUnit.SECONDS);
        }
    }

    private static void endMatches() {
        try {
            List<Match> endedMatches = matchService.endMatchesReadyToEnd();
            System.out.println("Matches ended: " + endedMatches.size());

            for (Match matches : endedMatches) {
                for (Team team : matches.getTeams()) {
                    for (Player player : team.getPlayers()) {
                        if (player.isAutoQueueOn()) {
                            matchmakingService.queuePlayer(player);

                            System.out.println("Player " + player.getId() + " auto queued.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static long getDelay(LocalDateTime nextMatchEndTime) {
        ZonedDateTime zdt = nextMatchEndTime.atZone(ZoneId.systemDefault());
        return zdt.toEpochSecond() - ZonedDateTime.now().toEpochSecond();
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
