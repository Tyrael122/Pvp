package org.example.pvp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class MatchGroup {
    @Id
    @GeneratedValue
    private long id;

    @OneToMany
    private List<MatchmakingProfile> matchmakingProfiles;

    public MatchGroup() {
    }

    public static MatchGroup of(List<MatchmakingProfile> matchmakingProfiles) {
        return new MatchGroup(matchmakingProfiles);
    }

    public MatchGroup(List<MatchmakingProfile> matchmakingProfiles) {
        this.matchmakingProfiles = new ArrayList<>(matchmakingProfiles);
    }

    public double calculateAverageRating() {
        if (matchmakingProfiles.isEmpty()) {
            return 0;
        }

        double sum = 0;

        for (MatchmakingProfile matchmakingProfile : matchmakingProfiles) {
            sum += matchmakingProfile.getRating();
        }

        return sum / matchmakingProfiles.size();
    }
}
