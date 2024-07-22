package org.example;

import org.example.draftactual.model.Team;
import org.example.draftactual.interfaces.WinnerCalculator;

import java.util.List;
import java.util.Scanner;

public class AskForWinnerCalculator implements WinnerCalculator {

    @Override
    public Team calculateWinner(List<Team> teams) {
        System.out.println("Who is the winner?");
        for (int i = 0; i < teams.size(); i++) {
            System.out.println(i + ". " + teams.get(i));
        }

        int winnerIndex = Integer.parseInt(readLine());
        System.out.println("Winner is " + teams.get(winnerIndex));

        return teams.get(winnerIndex);
    }

    public String readLine() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}
