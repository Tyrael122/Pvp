package org.example.pvp.matchmaking;

import jakarta.persistence.*;
import lombok.Data;
import org.example.pvp.model.MatchGroup;
import org.example.pvp.model.MatchmakingProfile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
class WaitingTeam {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<WaitingPlayer> waitingPlayers = new ArrayList<>();

    private double averageRating;

    public void bufferAverageRating() {
        if (waitingPlayers.isEmpty()) {
            averageRating = 0;
            return;
        }

        double sum = 0;

        for (WaitingPlayer player : waitingPlayers) {
            sum += player.getMatchmakingProfile().getRating();
        }

        averageRating = sum / waitingPlayers.size();
    }

    // TODO: Since this is used in the mergeTeams method in comparisons, it's result could be cached.
    public Duration calculateAverageWaitTime() {
        if (waitingPlayers.isEmpty()) {
            return Duration.of(0, ChronoUnit.SECONDS);
        }

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

    public void removePlayer(MatchmakingProfile matchmakingProfile) {
        waitingPlayers.removeIf(waitingPlayer -> waitingPlayer.getMatchmakingProfile().equals(matchmakingProfile));
    }

    public MatchGroup toTeam() {
        return MatchGroup.of(getPlayers());
    }

    public List<MatchmakingProfile> getPlayers() {
        return waitingPlayers.stream().map(WaitingPlayer::getMatchmakingProfile).toList();
    }
}
