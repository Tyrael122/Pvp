package org.example.pvp.interfaces;

import org.example.pvp.model.Team;

import java.util.List;

public interface EloRatingService { // Should this service define the player's ranking? Or just the rating?
    List<Team> updateRatings(List<Team> teams, Team winner);
}