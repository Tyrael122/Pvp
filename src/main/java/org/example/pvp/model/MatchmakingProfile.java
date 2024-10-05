package org.example.pvp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@AllArgsConstructor
@Builder(toBuilder = true)
public class MatchmakingProfile implements Cloneable {

    @Id
    @GeneratedValue
    private long id;

    private Division division;
    private double rating;

    private RankingMode rankingMode;

    @Builder.Default
    private boolean isAutoQueueOn = true;

    public MatchmakingProfile() {
        this.rating = 180;
    }

    public MatchmakingProfile(double rating) {
        this.rating = rating;
    }

    public MatchmakingProfile(RankingMode rankingMode) {
        this.rankingMode = rankingMode;
    }

    public MatchmakingProfile(long id, double rating) {
        this.id = id;
        this.rating = rating;
    }

    public MatchmakingProfile(long id, Division division, double rating) {
        this(id, rating);

        this.division = division;
    }

    @Override
    public MatchmakingProfile clone() {
        MatchmakingProfile matchmakingProfile;
        try {
            matchmakingProfile = (MatchmakingProfile) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        return matchmakingProfile.toBuilder().build();
    }
}