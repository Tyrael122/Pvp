package org.example.pvp.repositories;

import org.example.pvp.model.MatchmakingProfile;
import org.example.pvp.model.RankingMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRankingRepository extends JpaRepository<MatchmakingProfile, Long> {
    List<MatchmakingProfile> findAllByRankingMode(RankingMode rankingMode);
}
