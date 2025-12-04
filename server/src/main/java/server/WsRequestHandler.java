package server;

import chess.*;
import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ParticipationType;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadBoardMessage;
import websocket.messages.ServerErrorMessage;
import websocket.messages.ServerMessage;
import websocket.messages.ServerNotificationMessage;

import java.util.HashMap;
import java.util.HashSet;

public class WsRequestHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final UserService userService;
    private final HashMap<Integer, HashSet<UserConnection>> users;

    public WsRequestHandler(UserService userService) {
        this.userService = userService;
        this.users = new HashMap<>();
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Websocket connected!");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        var message = ctx.message();
        HashMap<String, String> map = new Gson().fromJson(message, HashMap.class);
        if (map.get("commandType").equals("MAKE_MOVE")) {
            makeMove(ctx);
        } else if (map.get("commandType").equals("CONNECT")) {
            try {
                connect(ctx);
            } catch (Exception e) {
                var error = new ServerErrorMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
                ctx.send(new Gson().toJson(error));
            }
        } else if (map.get("commandType").equals("LEAVE")) {
            leave(ctx);
        } else if (map.get("commandType").equals("RESIGN")) {
            resign(ctx);
        }
    }

    private void resign(WsMessageContext ctx) throws Exception {
        var message = ctx.message();
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        var connections = users.get(command.getGameID());

        var game = userService.getGame(command.getGameID());

        String user = userService.getUser(command.getAuthToken());
        var gameData = userService.getGame(command.getGameID());
        if (!((gameData.whiteAuthToken() != null &&
                gameData.whiteAuthToken().equals(command.getAuthToken())) ||
                (gameData.blackAuthToken() != null &&
                        gameData.blackAuthToken().equals(command.getAuthToken())
                ))) {
            var notification = new Gson().toJson(new ServerErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Error: An observer is not allowed to resign"));
            ctx.send(notification);
            return;
        }

        if (game.game().isGameOver()) {
            var notification = new Gson().toJson(new ServerErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Error: You cannot resign after the game is over"));
            ctx.send(notification);
            return;
        }

//        Mark the game as resigned
        userService.resignGame(game);

        var notification = new Gson().toJson(new ServerNotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                user + " resigned"));
        for (var con : connections) {
            try {
                con.ctx().send(notification);
            } catch (Exception e) {
                connections.remove(con);
            }
        }
    }

    private void leave(WsMessageContext ctx) throws Exception {
        ctx.closeSession();
//        Remove ctx from gameMap
        var message = ctx.message();
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        var connections = users.get(command.getGameID());

        var user = userService.getUser(command.getAuthToken());
        var notification = new Gson().toJson(new ServerNotificationMessage
                (ServerMessage.ServerMessageType.NOTIFICATION, user + " left the game"));

        var iterator = connections.iterator();
        while (iterator.hasNext()) {
            var con = iterator.next();
            if (con.ctx().equals(ctx)) {
                iterator.remove();
            } else {
                con.ctx().send(notification);
            }
        }

//        Remove user from color in game
        userService.removeUserFromGame(command.getAuthToken(), command.getGameID());
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        ctx.closeSession();
        System.out.println("Websocket closed!");
    }

    private void connect(WsMessageContext ctx) throws Exception {
        var message = ctx.message();
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        String user = userService.getUser(command.getAuthToken());
        GameData game;

        try {
            game = userService.getGame(command.getGameID());
        } catch (Exception e) {
            throw new Exception("Error: Requested game doesn't exist");
        }

        var observe = true;
        ParticipationType participationType = null;
        if (command.getAuthToken().equals(game.whiteAuthToken())) {
            observe = false;
            addUserToMap(ctx, ParticipationType.WHITE, command);
            participationType = ParticipationType.WHITE;
        } else if (command.getAuthToken().equals(game.blackAuthToken())) {
            observe = false;
            addUserToMap(ctx, ParticipationType.BLACK, command);
            participationType = ParticipationType.BLACK;
        }
        if (observe) {
            participationType = ParticipationType.OBSERVER;
            addUserToMap(ctx, ParticipationType.OBSERVER, command);
        }

        sendBoard(ctx, command);

        var connection = new UserConnection(ctx, participationType);
        var connections = users.get(command.getGameID());
        var notification = new Gson().toJson(new ServerNotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                user + " joined the game as " + participationType + "!"));

        var iterator = connections.iterator();
        while (iterator.hasNext()) {
            var con = iterator.next();
            if (!con.equals(connection)) {
//                    Try to send a message. If the connection was closed without a leave message,
//                    we simply remove the connection from our map
                try {
                    if (!con.ctx().session.isOpen()) {
                        iterator.remove();
                    } else {
                        con.ctx().send(notification);
                    }
                } catch (Exception e) {
                    iterator.remove();
                }
            }
        }

    }

    private void addUserToMap(WsMessageContext ctx, ParticipationType participationType, UserGameCommand command) {
        var connection = new UserConnection(ctx, participationType);
        if (users.containsKey(command.getGameID())) {
            users.get(command.getGameID()).add(connection);
        } else {
            users.put(command.getGameID(), new HashSet<>());
            users.get(command.getGameID()).add(connection);
        }
    }

    private void makeMove(WsMessageContext ctx) throws Exception {
        var message = ctx.message();
        MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);
        if (userService.getGame(command.getGameID()).game().isGameOver()) {
            var error = new ServerErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: No moves can be made after the game is ended");
            ctx.send(new Gson().toJson(error));
            return;
        }
//            Try to make the move. If fails, send a descriptive error to the client
        try {
            userService.makeMove(command);
        } catch (Exception e) {
            var error = new ServerErrorMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            ctx.send(new Gson().toJson(error));
            return;
        }

        GameData gameData;
        try {
            gameData = userService.getGame(command.getAuthToken(), command.getGameID());
        } catch (Exception e) {
            var error = new ServerErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: You are unauthorized to make that move");
            ctx.send(new Gson().toJson(error));
            return;
        }
        var loadBoardMessage = new LoadBoardMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
        var loadBoardMessageJson = new Gson().toJson(loadBoardMessage);

        var color = gameData.game().getTeamTurn() == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        var user = userService.getUser(command.getAuthToken());
        var moveNotification = new ServerNotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                user + ": " + color + " " + reverseParseMove(command.getMove()).toUpperCase());

        var connections = users.get(command.getGameID());
        for (var con : connections) {
//            Send the updated board to everyone
            con.ctx().send(loadBoardMessageJson);
//            Send the notification about the move to everyone else
            if (!con.ctx().session.equals(ctx.session)) {
                con.ctx().send(moveNotification);
            }
        }

//        Send message if check, checkmate, or stalemate
        var endGameNotification = specialGameNotification(gameData);
        if (endGameNotification == null) {
            return;
        }
        for (var con : connections) {
            con.ctx().send(new Gson().toJson(endGameNotification));
        }
    }

    private ServerNotificationMessage specialGameNotification(GameData gameData) {
        var game = gameData.game();
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            return new ServerNotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "WHITE is in checkmate!");
        } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            return new ServerNotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "BLACK is in checkmate!");
        } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            return new ServerNotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "WHITE is in check!");
        } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            return new ServerNotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "BLACK is in check!");
        } else if (game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            return new ServerNotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Stalemate!");
        }
        return null;
    }

    private String reverseParseMove(ChessMove move) {
        return reverseParsePosition(move.getStartPosition()) + " " + reverseParsePosition(move.getEndPosition());
    }

    private String reverseParsePosition(ChessPosition position) {
        char col = (char) (position.getColumn() + 96);
        char row = (char) (position.getRow() + '0');
        return new String(new char[]{col, row});
    }

    private void sendBoard(WsMessageContext ctx, UserGameCommand command) throws Exception {
        var gameData = userService.getGame(command.getGameID());
        var loadBoardMessage = new LoadBoardMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
        var loadBoardMessageJson = new Gson().toJson(loadBoardMessage);
        ctx.send(loadBoardMessageJson);
    }

}
