package org.example.pvp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Player {

    @Id
    @GeneratedValue
    private long id;

    @OneToMany
    private List<MatchmakingProfile> profiles;
}