package org.example.pvp.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Division {
    UNRANKED(0),
    BRONZE_1(1),
    BRONZE_2(2),
    SILVER_1(3),
    SILVER_2(4),
    GOLD_1(5),
    GOLD_2(6),
    PLATINUM_1(7),
    PLATINUM_2(8),
    DIAMOND(9),
    MASTER(10),
    GRANDMASTER(11);

    public static final List<Division> ASCENDING_DIVISIONS = buildAscendingDivisions();

    private final int id;

    Division(int id) {
        this.id = id;
    }

    private static List<Division> buildAscendingDivisions() {
        return Arrays.asList(Division.values());
    }
}
