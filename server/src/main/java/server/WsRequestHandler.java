package server;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.eclipse.jetty.server.Authentication;
import org.jetbrains.annotations.NotNull;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadBoardMessage;
import websocket.messages.ServerMessage;

import java.util.HashMap;

public class WsRequestHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final UserService userService;
    private HashMap<Integer, String> users;

    public WsRequestHandler(UserService userService) {
        this.userService = userService;
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
            MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);
//            Try to make the move. If fails, send a descriptive error to the client
            try {
                userService.makeMove(command);
            } catch (Exception e) {
                var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
                ctx.send(new Gson().toJson(error));
            }
        } else {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> {
                    var notification = new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                            command.getUsername() + " joined the game!"));
                    ctx.send(notification);
//                Send board to just the user
                    var gameData = userService.getGame(command.getAuthToken(), command.getGameID());
                    var loadBoardMessage = new LoadBoardMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game().getBoard());
                    var loadBoardMessageJson = new Gson().toJson(loadBoardMessage);
                    ctx.send(loadBoardMessageJson);
                }
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
}
