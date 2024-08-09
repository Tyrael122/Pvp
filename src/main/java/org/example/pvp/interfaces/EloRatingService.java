package org.example.pvp.interfaces;

import org.example.pvp.model.MatchGroup;

import java.util.List;

public interface EloRatingService {
    List<MatchGroup> updateRatings(List<MatchGroup> matchGroups, MatchGroup winner);
}