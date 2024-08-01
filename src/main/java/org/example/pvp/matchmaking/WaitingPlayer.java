package org.example.pvp.matchmaking;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.pvp.model.Player;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
class WaitingPlayer {
    private Player player;
    private LocalDateTime startWaitingTime;
}
