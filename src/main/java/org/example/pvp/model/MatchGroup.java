package org.example.pvp.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MatchGroup {

    private List<MatchmakingProfile> matchmakingProfiles;

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
