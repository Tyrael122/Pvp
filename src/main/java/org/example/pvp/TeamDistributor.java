package org.example.pvp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeamDistributor {

    static class Player {
        String name;
        int rating;

        Player(String name, int rating) {
            this.name = name;
            this.rating = rating;
        }

        @Override
        public String toString() {
            return name + " (Rating: " + rating + ")";
        }
    }

    public static void main(String[] args) {
        // Example list of players
        List<Player> players = new ArrayList<>();
        players.add(new Player("P1", 100));
        players.add(new Player("P2", 90));
        players.add(new Player("P3", 80));
        players.add(new Player("P4", 70));
        players.add(new Player("P5", 60));
        players.add(new Player("P6", 50));

        int numberOfTeams = 2;

        List<List<Player>> teams = distributePlayers(players, numberOfTeams);

        // Print out teams
        for (int i = 0; i < teams.size(); i++) {
            System.out.println("Team " + (i + 1) + ": " + teams.get(i));
        }
    }

    public static List<List<Player>> distributePlayers(List<Player> players, int numberOfTeams) {
        // Sort players by rating in descending order
        Collections.sort(players, (p1, p2) -> Integer.compare(p2.rating, p1.rating));

        // Initialize teams
        List<List<Player>> teams = new ArrayList<>();
        for (int i = 0; i < numberOfTeams; i++) {
            teams.add(new ArrayList<>());
        }

        // Distribute players into teams
        for (int i = 0; i < players.size(); i++) {
            teams.get(i % numberOfTeams).add(players.get(i));
        }

        return teams;
    }
}