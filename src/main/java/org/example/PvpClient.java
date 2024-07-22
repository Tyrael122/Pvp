package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class PvpClient {
    private PrintWriter toSocketWriter;
    private BufferedReader fromSocketReader;

    public void startConnection(String ip, int port) throws IOException {
        Socket clientSocket = new Socket(ip, port);
        toSocketWriter = new PrintWriter(clientSocket.getOutputStream(), true);

        fromSocketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void readMessage() {
        try {
            while (fromSocketReader.ready()) {
                System.out.println(fromSocketReader.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        toSocketWriter.println(msg);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Hello, PVP player!");
        System.out.println("Connecting to the server...");

        PvpClient client = new PvpClient();
        client.startConnection("localhost", 8080);

        System.out.println("Connected to the server!");

        while (true) {
            showMenu();

            String option = readLine();

            client.sendMessage(option);

            client.readMessage();
        }
    }

    private static String readLine() {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return readLine();
        }

        return line;
    }

    private static void showMenu() {
        System.out.println("Choose an option:");

        System.out.println("ME");
        System.out.println("MATCH");
        System.out.println("RANK");
    }
}
