package org.example.pvp.repositories;

import org.example.pvp.model.MatchGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<MatchGroup, Long> {
}
