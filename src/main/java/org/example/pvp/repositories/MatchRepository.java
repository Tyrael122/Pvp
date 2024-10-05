package org.example.pvp.repositories;

import org.example.pvp.model.Match;
import org.example.pvp.model.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByMatchStatus(MatchStatus matchStatus);
}
