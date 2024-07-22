package org.example.draftactual.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Match {
    private List<Team> teams;

    private Team winner;

    private MatchStatus matchStatus;

    private LocalDateTime startTime;
    private LocalDateTime scheduledEndTime;
    private LocalDateTime endTime;

    public Match(List<Team> teams) {
        this.teams = teams;
    }

    public void start(LocalDateTime scheduledEndTime) {
        this.startTime = LocalDateTime.now();
        this.matchStatus = MatchStatus.IN_PROGRESS;

        this.scheduledEndTime = scheduledEndTime;
    }

    public void draw() {
        end(null);
    }

    public void end(Team winner) {
        this.endTime = LocalDateTime.now();
        this.matchStatus = MatchStatus.FINISHED;
        this.winner = winner;
    }
}
