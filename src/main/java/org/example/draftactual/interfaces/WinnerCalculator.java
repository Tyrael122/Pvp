package org.example.draftactual.interfaces;

import org.example.draftactual.model.Team;

import java.util.List;

public interface WinnerCalculator {
    Team calculateWinner(List<Team> teams);
}
