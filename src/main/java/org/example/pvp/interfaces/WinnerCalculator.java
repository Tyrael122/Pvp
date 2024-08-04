package org.example.pvp.interfaces;

import org.example.pvp.model.MatchGroup;

import java.util.List;

public interface WinnerCalculator {
    MatchGroup calculateWinner(List<MatchGroup> matchGroups);
}
