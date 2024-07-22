package org.example.draftactual.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Getter
@With
@Builder(toBuilder = true)
public final class Player {
    private final long id;
    private final Rank rank;
    private final double rating;
    private final boolean isAutoQueueOn = true;

    public Player(long id) {
        this.id = id;
        this.rank = Rank.UNRANKED;
        this.rating = 1500;
    }

    public Player(long id, Rank rank, double rating) {
        this.id = id;
        this.rank = rank;
        this.rating = rating;
    }

    public Player clone() {
        Player player;
        try {
            player = (Player) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        return player.toBuilder().build();
    }
}