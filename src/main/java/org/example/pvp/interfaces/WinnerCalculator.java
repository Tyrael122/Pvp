package org.example.pvp.interfaces;

import org.example.pvp.model.Team;

import java.util.List;

public interface WinnerCalculator {
    Team calculateWinner(List<Team> teams);
}
