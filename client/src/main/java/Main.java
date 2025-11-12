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
        try {
            facade.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!(result.equals("quit") && state == LOGGED_OUT)) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                processInput(facade, line);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            result = line;
        }
        server.stop();
    }

    private static void processInput(ServerFacade facade, String input) throws Exception {
        String[] params = input.split(" ");
        String command = params[0];
        switch (state) {
            case LOGGED_OUT -> {
                switch (command.toLowerCase()) {
                    case "login" -> login(facade, params);
                    case "register" -> register(facade, params);
                    case "quit" -> {
                    }
                    default -> help();
                }
            }
            case LOGGED_IN -> {
                switch (command.toLowerCase()) {
                    case "logout" -> logout(facade);
                    case "create" -> createGame();
                    case "list" -> listGames();
                    case "play" -> playGame(facade, params);
                    case "observe" -> observeGame();
                    default -> help();
                }
            }
            case IN_GAME -> {
                System.out.println("Not yet developed");
            }
            default -> help();
        }
    }

    private static void observeGame() {
        System.out.println("Observing will be possible in phase 6");
    }

    private static void playGame(ServerFacade facade, String[] args) throws Exception {
        checkInputSize(3, args);

        String gameID = args[1];
        String color = args[2];

        int gameIDInt = 0;
        try {
            gameIDInt = Integer.parseInt(gameID);
        } catch (NumberFormatException e) {
            throw new Exception("Game ID must be a number");
        }

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new Exception("Color must be 'WHITE' or 'BLACK'");
        }

        facade.joinGame(color, gameIDInt);
    }

    private static void listGames() {

    }

    private static void createGame() {
    }

    private static void logout(ServerFacade facade) throws Exception {
        facade.logout();
        state = LOGGED_OUT;
    }

    private static void register(ServerFacade facade, String[] args) throws Exception {
        checkInputSize(4, args);

        String username = args[1];
        String password = args[2];
        String email = args[3];

        try {
            facade.register(username, password, email);
        } catch (Exception e) {
            System.out.println(e.getMessage());
//            System.out.println("An exception occurred during registration");
        }
    }

    private static void login(ServerFacade facade, String[] args) throws Exception {
        checkInputSize(3, args);

        String username = args[1];
        String password = args[2];

        facade.login(username, password);
        state = LOGGED_IN;
    }

    private static void help() {
//        Print out all the different help options
        String helpText;
        switch (state) {
            case LOGGED_OUT -> {
                helpText = """
                        help -- list possible commands
                        register <USERNAME> <PASSWORD> <EMAIL> -- register a new user
                        login <USERNAME> <PASSWORD> -- login an existing user
                        quit -- end the program
                        """;
            }
            case LOGGED_IN -> {
                helpText = """
                        help -- list possible commands
                        logout -- logout from the current user
                        create <NAME> -- create a new game
                        play <ID> [WHITE|BLACK] -- join a game
                        observe <ID> -- observe a game
                        list -- list all games
                        """;
            }
            case IN_GAME -> {
                helpText = """
                        Coming Soon!
                        """;
            }
            default -> {
                helpText = "A strange error has occurred";
            }
        }
        System.out.print(helpText);
    }

    private static void printPrompt() {
        String state_string = switch (state) {
            case LOGGED_OUT -> "[LOGGED OUT]";
            case LOGGED_IN -> "[LOGGED IN]";
            case IN_GAME -> "[GAME]";
            default -> "[ERROR]";
        };
        System.out.printf("%s >> ", state_string);
    }

    private static void checkInputSize(int size, String[] args) throws Exception {
        if (args.length != size) {
            throw new Exception("Please enter all necessary arguments");
        }
    }
}