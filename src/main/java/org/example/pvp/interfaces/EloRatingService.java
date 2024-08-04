package org.example.pvp.interfaces;

import org.example.pvp.model.MatchGroup;

import java.util.List;

public interface EloRatingService { // Should this service define the player's ranking? Or just the rating?
    List<MatchGroup> updateRatings(List<MatchGroup> matchGroups, MatchGroup winner);
}