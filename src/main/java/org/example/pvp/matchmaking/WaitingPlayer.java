package org.example.pvp.matchmaking;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.pvp.model.MatchmakingProfile;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
class WaitingPlayer {
    private MatchmakingProfile matchmakingProfile;
    private LocalDateTime startWaitingTime;
}
