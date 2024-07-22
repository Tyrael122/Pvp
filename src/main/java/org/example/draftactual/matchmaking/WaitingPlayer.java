package org.example.draftactual.matchmaking;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.draftactual.model.Player;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
class WaitingPlayer {
    private Player player;
    private LocalDateTime startWaitingTime;
}
