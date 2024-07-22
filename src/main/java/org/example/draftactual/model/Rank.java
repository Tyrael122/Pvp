package org.example.draftactual.model;

import lombok.Getter;

@Getter
public enum Rank {
    UNRANKED,
    BRONZE_1,
    BRONZE_2,
    BRONZE_3,
    SILVER_1,
    SILVER_2,
    SILVER_3,
    GOLD_1,
    GOLD_2,
    GOLD_3,
    PLATINUM_1,
    PLATINUM_2,
    PLATINUM_3,
    DIAMOND_1,
    DIAMOND_2,
    DIAMOND_3,
    MASTER,
    GRANDMASTER;

    private final boolean isDivision;

    Rank() {
        this(true);
    }

    Rank(boolean isDivision) {
        this.isDivision = isDivision;
    }
}