package org.example.pvp.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Rank {
    UNRANKED,
    BRONZE_1,
    BRONZE_2,
    SILVER_1,
    SILVER_2,
    GOLD_1,
    GOLD_2,
    PLATINUM_1,
    PLATINUM_2,
    DIAMOND,
    MASTER,
    GRANDMASTER;

    public static final List<Rank> ascendingRanks = buildAscendingRanks();

    private final boolean isDivision;

    Rank() {
        this(true);
    }

    Rank(boolean isDivision) {
        this.isDivision = isDivision;
    }

    private static List<Rank> buildAscendingRanks() {
        return Arrays.asList(Rank.values());
    }
}
