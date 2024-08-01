package org.example.pvp.matchmaking;

import lombok.Data;
import org.example.pvp.model.Player;
import org.example.pvp.model.Team;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
class WaitingTeam {

    private List<WaitingPlayer> waitingPlayers = new ArrayList<>();
    private double averageRating;

    public void bufferAverageRating() {
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

    public WaitingPlayer removePlayer(int i) {
        return waitingPlayers.remove(i);
    }

    public void removePlayer(Player player) {
        waitingPlayers.removeIf(waitingPlayer -> waitingPlayer.getPlayer().equals(player));
    }

    public Team toTeam() {
        return Team.of(getPlayers());
    }

    public List<Player> getPlayers() {
        return waitingPlayers.stream().map(WaitingPlayer::getPlayer).toList();
    }
}
