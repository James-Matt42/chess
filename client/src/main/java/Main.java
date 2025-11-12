import chess.*;
import client.ServerFacade;
import server.Server;

import java.util.Scanner;

public class Main {

    private static int state;

    static final int LOGGED_OUT = 0;
    static final int LOGGED_IN = 1;
    static final int IN_GAME = 2;

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        state = LOGGED_OUT;

        Server server = new Server();
        int port = server.run(8080);
        ServerFacade facade = new ServerFacade(port);

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!(result.equals("quit") && state == LOGGED_OUT)) {
            printPrompt();
            String line = scanner.nextLine();
            processInput(facade, line);
            System.out.format("You typed: %s\n", line);
            result = line;
        }
        server.stop();
    }

    private static void processInput(ServerFacade facade, String input) {
        String[] params = input.split(" ");
        String command = params[0];
        switch (state) {
            case LOGGED_OUT -> {
                switch (command.toLowerCase()) {
                    case "help" -> help();
                    case "login" -> login();
                    case "register" -> register(facade, params[1], params[2], params[3]);
                }
            }
            case LOGGED_IN -> {
                switch (command.toLowerCase()) {
                    case "help" -> help();
                    case "logout" -> logout();
                    case "create" -> createGame();
                    case "list" -> listGames();
                    case "play" -> playGame();
                    case "observe" -> observeGame();
                }
            }
            case IN_GAME -> {
                System.out.println("Not yet developed");
            }
            default -> help();
        }
    }

    private static void observeGame() {
    }

    private static void playGame() {
    }

    private static void listGames() {

    }

    private static void createGame() {
    }

    private static void logout() {
    }

    private static void register(ServerFacade facade, String username, String password, String email) {
        try {
            facade.register(username, password, email);
        } catch (Exception e) {
            System.out.println(e.getMessage());
//            System.out.println("An exception occurred during registration");
        }
    }

    private static void login() {
    }

    private static void help() {
//        Print out all the different help options
    }

    private static void printPrompt() {
        System.out.print(">> ");
    }
}