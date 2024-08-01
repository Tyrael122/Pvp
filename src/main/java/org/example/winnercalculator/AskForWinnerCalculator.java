package org.example.winnercalculator;

import org.example.pvp.model.Team;
import org.example.pvp.interfaces.WinnerCalculator;

import java.util.List;
import java.util.Scanner;

public class AskForWinnerCalculator implements WinnerCalculator {

    @Override
    public Team calculateWinner(List<Team> teams) {
        System.out.println("Who is the winner?");
        for (int i = 0; i < teams.size(); i++) {
            System.out.println(i + ". " + teams.get(i));
        }

        int winnerIndex = promptUtilValidInt();
        System.out.println("Winner is " + teams.get(winnerIndex));

        return teams.get(winnerIndex);
    }

    private Integer promptUtilValidInt() {
        String line = readLine();

        Integer result = tryParseInt(line);
        while (result == null) {
            System.out.println("Please enter a number.");
            line = readLine();
            result = tryParseInt(line);
        }

        return result;
    }

    public String readLine() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private Integer tryParseInt(String line) {
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
