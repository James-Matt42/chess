package server;

import chess.*;
import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import service.UserService;
import websocket.commands.ConnectCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ParticipationType;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadBoardMessage;
import websocket.messages.ServerMessage;

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
    public void handleConnect(@NotNull WsConnectContext ctx) throws Exception {
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
            connect(ctx, map);
        } else {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case LEAVE -> {
//                    var notification = new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
//                            command.getUsername() + " left the game"));
//                    ctx.send(notification);
                    ctx.closeSession();
                }
                case RESIGN -> {
//                    ctx.send(command.getUsername() + " has resigned");
                }
            }
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        ctx.closeSession();
        System.out.println("Websocket closed!");
    }

    private void connect(WsMessageContext ctx, HashMap<String, String> map) throws Exception {
        var message = ctx.message();
//        Necessary because the stupid tests require supporting UserGameCommand instead of my more useful ConnectCommand
//        that includes the username
        if (map.size() == 3) {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
            String user = userService.getUser(command.getAuthToken());
            var game = userService.getGame(command.getAuthToken(), command.getGameID());
            if (game == null) {
                throw new Exception("Requested game doesn't exist");
            }
            var observe = true;
            if (user.equals(game.whiteUsername())) {
                observe = false;
                addUserToMap(ctx, ParticipationType.WHITE, command);
            }
            if (user.equals(game.blackUsername())) {
                observe = false;
                addUserToMap(ctx, ParticipationType.BLACK, command);
            }
            if (observe) {
                addUserToMap(ctx, ParticipationType.OBSERVER, command);
            }
            sendBoard(ctx, command);

            var connections = users.get(command.getGameID());
            var notification = new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    user + " joined the game as " + participationType + "!"));
            for (var con : connections) {
                if (!con.equals(connection)) {
                    con.ctx().send(notification);
                }
            }
        }

        ConnectCommand command = new Gson().fromJson(message, ConnectCommand.class);

//            Verify that the user is authorized to play white or black
//            Otherwise, the player is an observer, which is fine
        var game = userService.getGame(command.getAuthToken(), command.getGameID());
        if (game == null) {
            throw new Exception("Requested game doesn't exist");
        }
        ParticipationType participationType = getParticipationType(command, game);


        var connection = new UserConnection(ctx, participationType);
        if (users.containsKey(command.getGameID())) {
            users.get(command.getGameID()).add(connection);
        } else {
            users.put(command.getGameID(), new HashSet<>());
            users.get(command.getGameID()).add(connection);
        }

        sendBoard(ctx, command);

        var connections = users.get(command.getGameID());
        var notification = new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                command.getUsername() + " joined the game as " + participationType.toString() + "!"));
        for (var con : connections) {
            if (!con.equals(connection)) {
                con.ctx().send(notification);
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

    @NotNull
    private static ParticipationType getParticipationType(ConnectCommand command, GameData game) throws Exception {
        ParticipationType participationType;
        if (command.getParticipationType() != null) {
            if (command.getParticipationType().equals(ParticipationType.WHITE)) {
                if (!command.getUsername().equals(game.whiteUsername())) {
                    throw new Exception("You are not authorized to play as WHITE");
                }
                participationType = ParticipationType.WHITE;
            } else if (command.getParticipationType().equals(ParticipationType.BLACK)) {
                if (!command.getUsername().equals(game.blackUsername())) {
                    throw new Exception("You are not authorized to play as BLACK");
                }
                participationType = ParticipationType.BLACK;
            } else {
                participationType = ParticipationType.OBSERVER;
            }
        } else {
            participationType = ParticipationType.OBSERVER;
        }
        return participationType;
    }

    private void makeMove(WsMessageContext ctx) throws Exception {
        var message = ctx.message();
        MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);
//            Try to make the move. If fails, send a descriptive error to the client
        try {
            userService.makeMove(command);
        } catch (Exception e) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            ctx.send(new Gson().toJson(error));
        }

        var gameData = userService.getGame(command.getAuthToken(), command.getGameID());
        var loadBoardMessage = new LoadBoardMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game().getBoard());
        var loadBoardMessageJson = new Gson().toJson(loadBoardMessage);

        var color = gameData.game().getTeamTurn() == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        var moveNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                color + " " + reversParseMove(command.getMove()).toUpperCase());

        var connections = users.get(command.getGameID());
        for (var con : connections) {
//            Send the updated board to everyone
            con.ctx().send(loadBoardMessageJson);
//            Send the notification about the move to everyone else
            if (!con.ctx().session.equals(ctx.session)) {
                con.ctx().send(moveNotification);
            }
        }
    }

    private String reversParseMove(ChessMove move) {
        return reverseParsePosition(move.getStartPosition()) + " " + reverseParsePosition(move.getEndPosition());
    }

    private String reverseParsePosition(ChessPosition position) {
        char col = (char) (position.getColumn() + 96);
        char row = (char) (position.getRow() + '0');
        return new String(new char[]{col, row});
    }

    private void sendBoard(WsMessageContext ctx, UserGameCommand command) throws Exception {
        var gameData = userService.getGame(command.getAuthToken(), command.getGameID());
        var loadBoardMessage = new LoadBoardMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game().getBoard());
        var loadBoardMessageJson = new Gson().toJson(loadBoardMessage);
        ctx.send(loadBoardMessageJson);
    }

    private void sendBoard(WsMessageContext ctx, MakeMoveCommand command) throws Exception {
        var gameData = userService.getGame(command.getAuthToken(), command.getGameID());
        var loadBoardMessage = new LoadBoardMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game().getBoard());
        var loadBoardMessageJson = new Gson().toJson(loadBoardMessage);
        ctx.send(loadBoardMessageJson);
    }
}
