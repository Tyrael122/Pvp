package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.pvp.model.Player;
import org.example.pvp.repositories.PlayerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
public class PlayerController {

    private final String ENDPOINT_PREFIX = "/player";

    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @PostMapping(ENDPOINT_PREFIX)
    public ResponseEntity<Player> createPlayer() {
        Player player = new Player();
        playerRepository.save(player);

        return ResponseEntity.ok(player);
    }

    @PostMapping(ENDPOINT_PREFIX + "/{playerId}")
    public ResponseEntity<Player> getPlayer(@PathVariable Long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow();

        return ResponseEntity.ok(player);
    }
}