package org.example.draftactual.matchmaking;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.example.draftactual.model.Player;
import org.example.draftactual.model.Team;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
class WaitingTeam {

    @Setter(AccessLevel.NONE)
    private List<WaitingPlayer> waitingPlayers = new ArrayList<>();

    private double averageRating;

    public void addPlayer(WaitingPlayer player) {
        addPlayers(List.of(player));
    }

    public void addPlayers(List<WaitingPlayer> players) {
        waitingPlayers.addAll(players);

        calculateAverageRating();
    }

    public WaitingPlayer removePlayer(int i) {
        WaitingPlayer player = waitingPlayers.remove(i);

        calculateAverageRating();

        return player;
    }

    private void calculateAverageRating() {
        double sum = 0;

        for (WaitingPlayer player : waitingPlayers) {
            sum += player.getPlayer().getRating();
        }

        averageRating = sum / waitingPlayers.size();
    }

    // TODO: Since this is used in the mergeTeams method in comparisons, it's result could be cached.
    public Duration calculateAverageWaitTime() {
        Duration sum = Duration.of(0, ChronoUnit.SECONDS);

        LocalDateTime now = LocalDateTime.now();

        for (WaitingPlayer player : waitingPlayers) {
            Duration waitTime = Duration.between(player.getStartWaitingTime(), now);
            sum = sum.plus(waitTime);
        }

        return sum.dividedBy(waitingPlayers.size());
    }

    public Team toTeam() {
        return Team.of(getWaitingPlayers().stream().map(WaitingPlayer::getPlayer).toList());
    }

    public boolean containsPlayer(Player player) {
        return waitingPlayers.stream().anyMatch(waitingPlayer -> waitingPlayer.getPlayer().equals(player));
    }
}
