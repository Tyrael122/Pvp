package org.example.draftactual.model;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class Team {

    private List<Player> players;

    public static Team of(List<Player> players) {
        return new Team(players);
    }

    public Team(List<Player> players) {
        this.players = players;
    }

    public double calculateAverageRating() {
        if (players.isEmpty()) {
            return 0;
        }

        double sum = 0;

        for (Player player : players) {
            sum += player.getRating();
        }

        return sum / players.size();
    }
}
