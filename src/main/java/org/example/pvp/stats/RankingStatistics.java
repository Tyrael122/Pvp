package org.example.pvp.stats;

import org.example.pvp.model.Division;

import java.util.Map;

public record RankingStatistics(Map<Division, Integer> rankingsSize, double averageRating) {
}
