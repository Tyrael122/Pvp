package org.example.pvp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Player {

    @Id
    @GeneratedValue
    private long id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<MatchmakingProfile> profiles;

    public MatchmakingProfile findMatchmakingProfileByRankingMode(RankingMode rankingMode) {
        return profiles.stream()
                .filter(profile -> profile.getRankingMode().equals(rankingMode))
                .findFirst()
                .orElse(null);
    }
}