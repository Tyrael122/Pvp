package org.example.pvp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Player implements Cloneable {
    private final long id;
    private Rank rank;
    private double rating;

    @Builder.Default
    private boolean isAutoQueueOn = true;

    public Player(long id) {
        this.id = id;
        this.rank = Rank.UNRANKED;
        this.rating = 180;
    }

    public Player(long id, double rating) {
        this(id);

        this.rating = rating;
    }

    public Player(long id, Rank rank, double rating) {
        this(id, rating);

        this.rank = rank;
    }

    @Override
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