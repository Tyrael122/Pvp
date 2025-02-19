package org.example.pvp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Match {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<MatchGroup> matchGroups;

    @OneToOne
    private MatchGroup winner;

    private MatchStatus matchStatus;

    private LocalDateTime startTime;
    private LocalDateTime scheduledEndTime;
    private LocalDateTime endTime;

    public Match() {
    }

    public Match(List<MatchGroup> matchGroups) {
        this.matchGroups = new ArrayList<>(matchGroups);
    }

    public void start(LocalDateTime scheduledEndTime) {
        this.startTime = LocalDateTime.now();
        this.matchStatus = MatchStatus.IN_PROGRESS;

        this.scheduledEndTime = scheduledEndTime;
    }

    public void end(MatchGroup winner) {
        this.endTime = LocalDateTime.now();
        this.matchStatus = MatchStatus.FINISHED;
        this.winner = winner;
    }
}
