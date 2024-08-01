package org.example.pvp.stats;

import org.example.pvp.model.Rank;

import java.util.Map;

public record RankingStatistics(Map<Rank, Integer> rankingsSize, double averageRating) {
}
