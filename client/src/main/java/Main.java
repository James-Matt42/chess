import chess.*;
import client.DrawBoard;
import client.ServerFacade;
import chess.ChessGame.TeamColor;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.util.*;

import static ui.EscapeSequences.*;

public class Main {

    private static int state;
    private static TeamColor playerColor;
    private static ChessBoard board;
    private static DrawBoard drawBoard;
    static LinkedHashMap<Integer, GameData> gameMap;
    private static int currGameID;
    private static boolean observer;

    static final int LOGGED_OUT = 0;
    static final int LOGGED_IN = 1;
    static final int IN_GAME = 2;

    public static void main(String[] args) throws Exception {
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
                switch (command.toLowerCase()) {
                    case "move" -> makeMove(facade, args);
                    case "redraw" -> drawBoard(facade);
                    case "resign" -> resignGame(facade);
                    case "leave" -> leaveGame(facade);
                    case "highlight" -> highlightMoves(args);
                    default -> help();
                }
            }
            default -> help();
        }
    }

    private static void makeMove(ServerFacade facade, String[] args) throws Exception {
        if (observer) {
            throw new Exception("You cannot move as an observer");
        }
        checkInputSize(3, args);

        var startPos = parsePosition(args[1]);
        var endPos = parsePosition(args[2]);

        var board = facade.getBoard();
        if (board == null) {
            throw new Exception("The board has not yet arrived... Please wait");
        }
        var startPiece = board.getPiece(startPos);
        if (startPiece == null) {
            throw new Exception("You can only select an existing piece");
        }
        if (!playerColor.equals(startPiece.getTeamColor())) {
            throw new Exception("You can only move a piece of your own color");
        }

        var possibleMoves = board.getColorMoves(playerColor);
        validateMove(startPos, endPos, possibleMoves);
        boolean promote = shouldPromote(possibleMoves);

        ChessMove move;
        if (promote) {
            move = new ChessMove(startPos, endPos, getPromotionPiece());
        } else {
            move = new ChessMove(startPos, endPos, null);
        }

        facade.makeMove(move, currGameID);
    }

    private static void validateMove(ChessPosition startPos, ChessPosition endPos, Collection<ChessMove> moves) throws Exception {
        for (var move : moves) {
            if (startPos.equals(move.getStartPosition()) && endPos.equals(move.getEndPosition())) {
                return;
            }
        }

        throw new Exception("Please enter a valid move");
    }

    private static ChessPiece.PieceType getPromotionPiece() {
        System.out.print("""
                Choose your promotion piece:
                'Q' -> Queen
                'K' -> Knight
                'R' -> Rook
                'B' -> Bishop
                >> 
                """);
        Scanner scanner = new Scanner(System.in);
        String piece = scanner.nextLine().toUpperCase();
        ChessPiece.PieceType pieceType;
        var validPieceTypes = List.of("Q", "K", "R", "B");

        while (!validPieceTypes.contains(piece)) {
            System.out.print("""
                     Please enter a valid option.
                     >> 
                    """);
            piece = scanner.nextLine().toUpperCase();
        }
        switch (piece) {
            case "K" -> pieceType = ChessPiece.PieceType.KNIGHT;
            case "R" -> pieceType = ChessPiece.PieceType.ROOK;
            case "B" -> pieceType = ChessPiece.PieceType.BISHOP;
            default -> pieceType = ChessPiece.PieceType.QUEEN;
        }
        return pieceType;
    }

    private static boolean shouldPromote(Collection<ChessMove> moves) {
        for (var move : moves) {
            if (move.getPromotionPiece() == null) {
                return false;
            }
        }
        return true;
    }

    private static ChessPosition parsePosition(String pos) throws Exception {
        var moveException = new Exception("Please enter a valid move");

//        Check the length
        if (pos.length() != 2) {
            throw moveException;
        }

//        Parse col value
        int col;
        try {
            char colChar = pos.toLowerCase().charAt(0);
            col = colChar - 96;
        } catch (Exception e) {
            throw moveException;
        }

//        Parse row value
        int row;
        try {
            row = Integer.parseInt(pos.substring(1));
        } catch (Exception e) {
            throw moveException;
        }

        return new ChessPosition(row, col);
    }

    private static void highlightMoves(String[] args) {
    }

    private static void leaveGame(ServerFacade facade) throws IOException {
        facade.leaveGame(currGameID);
        state = LOGGED_IN;
    }

    private static void resignGame(ServerFacade facade) throws IOException {
        facade.resignGame(currGameID);
    }

    private static void sendCommand(ServerFacade facade, UserGameCommand command) throws Exception {
//        Send the command to the server
    }

    private static void observeGame(ServerFacade facade, String[] args) throws Exception {
        checkInputSize(2, args);

        int gameID;
        try {
            gameID = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new Exception("Game ID must be a number");
        }

        boolean found = false;
        for (var id : gameMap.keySet()) {
            if (id == gameID) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new Exception("Game ID must be for an existing game");
        }

        playerColor = TeamColor.WHITE;
        currGameID = gameMap.get(gameID).gameID();
        facade.observeGame(gameID);
        state = IN_GAME;
        observer = true;
    }

    private static void playGame(ServerFacade facade, String[] args) throws Exception {
        checkInputSize(3, args);

        String gameID = args[1];
        String color = args[2].toUpperCase();

        int gameIDInt;
        try {
            gameIDInt = Integer.parseInt(gameID);
        } catch (NumberFormatException e) {
            throw new Exception("Game ID must be a number");
        }

        boolean found = false;
        for (var id : gameMap.keySet()) {
            if (id == gameIDInt) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new Exception("Game ID must be for an existing game");
        }

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new Exception("Color must be 'WHITE' or 'BLACK'");
        }

        currGameID = gameMap.get(gameIDInt).gameID();
        facade.joinGame(color, currGameID);
        state = IN_GAME;
        if (color.equals("WHITE")) {
            playerColor = TeamColor.WHITE;
        } else {
            playerColor = TeamColor.BLACK;
        }
        observer = false;
    }

    private static void listGames(ServerFacade facade) throws Exception {
        getGames(facade);
        if (gameMap.isEmpty()) {
            System.out.println("No games. Create a game to get started!");
        }
        for (var game : gameMap.keySet()) {
            String whiteUser = gameMap.get(game).whiteUsername();
            String blackUser = gameMap.get(game).blackUsername();
            if (whiteUser == null) {
                whiteUser = "[OPEN]";
            }
            if (blackUser == null) {
                blackUser = "[OPEN]";
            }
            System.out.printf("%d. Game: %s\t|\tGame ID: %d\t|\tWhite user: %s\t|\tBlack user: %s%n", game, gameMap.get(game).gameName(), game, whiteUser, blackUser);
        }
    }

    private static void createGame(ServerFacade facade, String[] args) throws Exception {
        checkInputSize(2, args);

        String gameName = args[1].strip();

        int gameID = facade.createGame(gameName);
        System.out.printf("Game '%s' created successfully%n", gameName);
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
                    help -- list possible commands
                    redraw -- redraw the current chess board
                    leave -- immediately leave the game
                    move <a-h><1-8> <a-h><1-8> -- make a move (e.g. move e2 e4)
                    resign -- forfeit the game
                    highlight <a-h><1-8> -- highlight the possible moves of a piece
                    """;
            default -> helpText = "A strange error has occurred";
        }
        System.out.print(helpText);
    }

    public static void printPrompt() {
        String stateString = switch (state) {
            case LOGGED_OUT -> "[LOGGED OUT]";
            case LOGGED_IN -> "[LOGGED IN]";
            case IN_GAME -> "[GAME]";
            default -> "[ERROR]";
        };
        System.out.printf("%s >> ", stateString);
    }

    private static void checkInputSize(int size, String[] args) throws Exception {
        if (args.length != size) {
            throw new Exception("Please enter a valid command (Type 'help' for commands/arguments)");
        }
    }

    private static void drawBoard(ServerFacade facade) {
        DrawBoard.drawBoard(facade.getBoard(), playerColor);
    }

    private static void getGames(ServerFacade facade) throws Exception {
        var games = facade.listGames();
        gameMap = new LinkedHashMap<>();
        for (int i = 0; i < games.size(); i++) {
            gameMap.put(i + 1, games.get(i));
        }
    }
}
