import chess.*;
import client.ServerFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Main {

    private static int state;

    static final int LOGGED_OUT = 0;
    static final int LOGGED_IN = 1;
    static final int IN_GAME = 2;

    public static void main(String[] args) {
        state = LOGGED_OUT;

        ServerFacade facade = new ServerFacade(8080);

        System.out.println(WHITE_QUEEN + " 240 Chess Client: " + BLACK_QUEEN);
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
    }

    private static void processInput(ServerFacade facade, String input) throws Exception {
        String[] args = input.split(" ");
        String command = args[0];

        if (command.equals("board")) {
            drawBoard("WHITE", new ChessGame().getBoard());
        }

        switch (state) {
            case LOGGED_OUT -> {
                switch (command.toLowerCase()) {
                    case "login" -> login(facade, args);
                    case "register" -> register(facade, args);
                    case "quit" -> {
                    }
                    default -> help();
                }
            }
            case LOGGED_IN -> {
                switch (command.toLowerCase()) {
                    case "logout" -> logout(facade);
                    case "create" -> createGame(facade, args);
                    case "list" -> listGames(facade);
                    case "play" -> playGame(facade, args);
                    case "observe" -> observeGame(facade, args);
                    default -> help();
                }
            }
            case IN_GAME -> {
                if (command.equalsIgnoreCase("quit")) {
                    state = LOGGED_IN;
                } else {
                    System.out.println("Not yet developed, type 'quit' to quit");
                }
            }
            default -> help();
        }
    }

    private static void observeGame(ServerFacade facade, String[] args) throws Exception {
        checkInputSize(2, args);

        int gameID;
        try {
            gameID = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new Exception("Game ID must be a number");
        }

        var games = facade.listGames();
        boolean found = false;
        for (var game : games) {
            if (game.gameID() == gameID) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new Exception("Game ID must be for an existing game");
        }

        drawBoard("WHITE", new ChessGame().getBoard());

        System.out.println("Observing will be possible in phase 6");
    }

    private static void playGame(ServerFacade facade, String[] args) throws Exception {
        checkInputSize(3, args);

        String gameID = args[1];
        String color = args[2];

        int gameIDInt;
        try {
            gameIDInt = Integer.parseInt(gameID);
        } catch (NumberFormatException e) {
            throw new Exception("Game ID must be a number");
        }

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new Exception("Color must be 'WHITE' or 'BLACK'");
        }

        facade.joinGame(color, gameIDInt);
        state = IN_GAME;

        drawBoard(color, new ChessGame().getBoard());
    }

    private static void listGames(ServerFacade facade) throws Exception {
        var games = facade.listGames();
        for (var game : games) {
            System.out.printf("Game: %s\tGame ID: %d%n", game.gameName(), game.gameID());
        }
    }

    private static void createGame(ServerFacade facade, String[] args) throws Exception {
        checkInputSize(2, args);

        String gameName = args[1].strip();

        int gameID = facade.createGame(gameName);
        System.out.printf("Game '%s' created successfully with game ID = %d%n", gameName, gameID);
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
            facade.login(username, password);
            state = LOGGED_IN;
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
            case LOGGED_OUT -> helpText = """
                    help -- list possible commands
                    register <USERNAME> <PASSWORD> <EMAIL> -- register a new user
                    login <USERNAME> <PASSWORD> -- login an existing user
                    quit -- end the program
                    """;
            case LOGGED_IN -> helpText = """
                    help -- list possible commands
                    logout -- logout the current user
                    create <NAME> -- create a new game
                    play <ID> [WHITE|BLACK] -- join a game
                    observe <ID> -- observe a game
                    list -- list all games
                    """;
            case IN_GAME -> helpText = """
                    Coming Soon!
                    """;
            default -> helpText = "A strange error has occurred";
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
            throw new Exception("Please enter a valid command (Type 'help' for commands/arguments)");
        }
    }

    private static void drawBoard(String color, ChessBoard board) {
        final String borderColor = SET_BG_COLOR_DARK_GREEN;
        int start = 8;
        int stop = 0;
        int step = -1;
        if (Objects.equals(color, "BLACK")) {
            start = 1;
            stop = 9;
            step = 1;
        }

        StringBuilder boardString = new StringBuilder();

        String topBottomRow = getTopBottomRow(color, borderColor);
        boardString.append(topBottomRow);

        for (int i = start; i != stop; i += step) {
            boardString.append(borderColor).append(" ").append(i).append(" ");
            for (int j = 1; j < 9; j++) {
                var piece = board.getPiece(new ChessPosition(i, j));
                boardString.append(getBGColor(i, j)).append(getPieceType(piece));
            }
            boardString.append(borderColor).append(" ").append(i).append(" ").append(RESET_BG_COLOR).append("\n");
        }

        boardString.append(topBottomRow);

        System.out.println(boardString);
    }

    private static String getTopBottomRow(String color, String borderColor) {
        ArrayList<String> rowChars = new ArrayList<>(List.of(" A ", " B ", " C ", " D ", " E ", " F ", " G ", " H "));
        if (Objects.equals(color, "BLACK")) {
            rowChars = new ArrayList<>(rowChars.reversed());
        }

        StringBuilder topBottomRow = new StringBuilder();
        topBottomRow.append(borderColor).append(EMPTY);
        for (var c : rowChars) {
            topBottomRow.append(c);
        }
        topBottomRow.append(EMPTY + RESET_BG_COLOR + "\n");

        return topBottomRow.toString();
    }

    private static String getBGColor(int i, int j) {
        if ((i + j) % 2 == 0) {
            return SET_BG_COLOR_DARK_GREY;
        }
        return SET_BG_COLOR_LIGHT_GREY;
    }

    private static String getPieceType(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }

        return switch (piece.getTeamColor()) {
            case WHITE -> {
                switch (piece.getPieceType()) {
                    case KING -> {
                        yield WHITE_KING;
                    }
                    case QUEEN -> {
                        yield WHITE_QUEEN;
                    }
                    case BISHOP -> {
                        yield WHITE_BISHOP;
                    }
                    case KNIGHT -> {
                        yield WHITE_KNIGHT;
                    }
                    case ROOK -> {
                        yield WHITE_ROOK;
                    }
                    case PAWN -> {
                        yield WHITE_PAWN;
                    }
                    default -> {
                        yield "";
                    }
                }
            }
            case BLACK -> {
                switch (piece.getPieceType()) {
                    case KING -> {
                        yield BLACK_KING;
                    }
                    case QUEEN -> {
                        yield BLACK_QUEEN;
                    }
                    case BISHOP -> {
                        yield BLACK_BISHOP;
                    }
                    case KNIGHT -> {
                        yield BLACK_KNIGHT;
                    }
                    case ROOK -> {
                        yield BLACK_ROOK;
                    }
                    case PAWN -> {
                        yield BLACK_PAWN;
                    }
                    default -> {
                        yield "";
                    }
                }
            }
        };
    }
}