package server;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.eclipse.jetty.server.Authentication;
import org.jetbrains.annotations.NotNull;
import service.UserService;
import websocket.commands.ConnectCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ParticipationType;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadBoardMessage;
import websocket.messages.ServerMessage;

import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

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
        var map = new Gson().fromJson(message, HashMap.class);
        if (map.get("commandType").equals("MAKE_MOVE")) {
            makeMove(ctx);
        } else if (map.get("commandType").equals("CONNECT")) {
            connect(ctx);
        } else {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case LEAVE -> {
                    var notification = new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                            command.getUsername() + " left the game"));
                    ctx.send(notification);
                    ctx.closeSession();
                }
                case RESIGN -> {
                    ctx.send(command.getUsername() + " has resigned");
                }
            }
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        ctx.closeSession();
        System.out.println("Websocket closed!");
    }

    private void connect(WsMessageContext ctx) throws Exception {
        var message = ctx.message();
        ConnectCommand command = new Gson().fromJson(message, ConnectCommand.class);

//            Verify that the user is authorized to play white or black
//            Otherwise, the player is an observer, which is fine
        var game = userService.getGame(command.getAuthToken(), command.getGameID());
        if (game == null) {
            throw new Exception("Requested game doesn't exist");
        }
        if (command.getParticipationType().equals(ParticipationType.WHITE)) {
            if (!command.getUsername().equals(game.whiteUsername())) {
                throw new Exception("You are not authorized to play as WHITE");
            }
        } else if (command.getParticipationType().equals(ParticipationType.BLACK)) {
            if (!command.getUsername().equals(game.blackUsername())) {
                throw new Exception("You are not authorized to play as BLACK");
            }
        }

        var connection = new UserConnection(ctx.sessionId(), ctx.session, command.getParticipationType());
        if (users.containsKey(command.getGameID())) {
            users.get(command.getGameID()).add(connection);
        } else {
            users.put(command.getGameID(), new HashSet<>());
            users.get(command.getGameID()).add(connection);
        }

        sendBoard(ctx, command);

        var connections = users.get(command.getGameID());
        var notification = new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                command.getUsername() + " joined the game as " + command.getParticipationType().toString() + "!"));
        for (var con : connections) {
            if (!con.equals(connection)) {
                new WsMessageContext(con.sessionId(), con.session(), "").send(notification);
            }
        }
    }

    private void makeMove(WsMessageContext ctx) {
        var message = ctx.message();
        MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);
//            Try to make the move. If fails, send a descriptive error to the client
        try {
            userService.makeMove(command);
            sendBoard(ctx, command);
        } catch (Exception e) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            ctx.send(new Gson().toJson(error));
        }
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
