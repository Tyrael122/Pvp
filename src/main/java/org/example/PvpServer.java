package org.example;

import lombok.SneakyThrows;
import org.example.draftactual.interfaces.MatchService;
import org.example.draftactual.interfaces.MatchmakingService;
import org.example.draftactual.interfaces.RankingService;
import org.example.draftactual.model.Player;
import org.example.draftactual.model.Rank;
import org.example.draftactual.model.Team;
import org.example.draftactual.matchmaking.VersusMatchmakingService;
import org.example.draftactual.services.VersusRankingService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PvpServer {
    private static final RankingService rankingService = new VersusRankingService();

    private static final MatchmakingService matchmakingService = new VersusMatchmakingService();
    private static final MatchService matchService = null;

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);

        while (true) {
            Socket clientSocket = serverSocket.accept();

            PrintWriter toClientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Player player = new Player();

            matchmakingService.queuePlayer(player);
            while (matchmakingService.isMatchReady()) {
                List<Team> teams = matchmakingService.fetchTeamsForMatch();
                matchService.startMatch(teams);

                scheduleNewThreadToEndMatch();
            }

            new PvpClientHandler(player, toClientOut, reader).start();
        }
    }

    private void scheduleNewThreadToEndMatch() {
        LocalDateTime nextMatchEndTime = PvpServer.matchService.calculateNextMatchEndTime();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(PvpServer.matchService::endMatchesReadyToEnd, getDelay(nextMatchEndTime), TimeUnit.SECONDS);
    }

    private static long getDelay(LocalDateTime nextMatchEndTime) {
        ZonedDateTime zdt = nextMatchEndTime.atZone(ZoneId.systemDefault());
        return zdt.toEpochSecond() - ZonedDateTime.now().toEpochSecond();
    }

    public static void main(String[] args) {
        PvpServer pvpServer = new PvpServer();
        try {
            pvpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class PvpClientHandler extends Thread {
        private final Player player;

        private final PrintWriter toClientOut;
        private final BufferedReader fromClientReader;

        public PvpClientHandler(Player player, PrintWriter toClientOut, BufferedReader fromClientReader) {
            this.toClientOut = toClientOut;
            this.fromClientReader = fromClientReader;
            this.player = player;
        }

        @SneakyThrows
        public void run() {
            String message = fromClientReader.readLine();
            try {
                while (message != null) {
                    if (!MenuOption.isValid(message.toUpperCase())) {
                        toClientOut.println("Invalid option");
                        message = fromClientReader.readLine();

                        continue;
                    }

                    switchOptions(MenuOption.valueOf(message.toUpperCase()));

                    message = fromClientReader.readLine();
                }

            } catch (IOException e) {
            }
        }

        private void switchOptions(MenuOption option) {
            switch (option) {
                case ME:
                    toClientOut.println("Your id: " + player.getId() + "\nYour rank: " + player.getRank() + "\nYour rating: " + player.getRating());
                    break;
                case MATCH:
                    toClientOut.println("Current match: " + matchService.getPlayerMatch(player));
                    break;
                case RANK:
                    toClientOut.println("Current ranking: " + buildRankingList());
                    break;
                default:
                    toClientOut.println("Invalid option");
            }
        }

        private String buildRankingList() {
            StringBuilder rankingList = new StringBuilder();

            Rank[] ranks = Rank.values();
            for (Rank rank : ranks) {
                rankingList.append(rank).append(":\n");

                for (Player player : rankingService.getRanking(rank)) {
                    rankingList.append(player.getId()).append(" - ").append(" - ").append(player.getRating()).append("\n");
                }
            }

            return rankingList.toString();
        }
    }
}
