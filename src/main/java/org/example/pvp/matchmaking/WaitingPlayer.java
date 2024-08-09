package org.example.pvp.matchmaking;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.pvp.model.MatchmakingProfile;

import java.time.LocalDateTime;


@Data
@Entity
class WaitingPlayer {
    @Id
    @GeneratedValue
    private long id;

    @OneToOne
    private MatchmakingProfile matchmakingProfile;

    private LocalDateTime startWaitingTime;

    public WaitingPlayer() {
    }

    public WaitingPlayer(MatchmakingProfile matchmakingProfile, LocalDateTime startWaitingTime) {
        this.matchmakingProfile = matchmakingProfile;
        this.startWaitingTime = startWaitingTime;
    }
}
