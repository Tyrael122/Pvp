package org.example.draftactual.interfaces;

import org.example.draftactual.model.Team;

import java.util.List;

public interface EloRatingService { // Should this service define the player's ranking? Or just the rating?
    List<Team> updateRatings(List<Team> teams, Team winner);
}