package server;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.eclipse.jetty.server.Authentication;
import org.jetbrains.annotations.NotNull;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

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
        ctx.send("Some user joined the game");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        var message = ctx.message();
        var map = new Gson().fromJson(message, HashMap.class);
        UserGameCommand command;
        if (map.containsKey("MAKE_MOVE")) {
            command = new Gson().fromJson(message, MakeMoveCommand.class);
        } else {
            command = new Gson().fromJson(message, UserGameCommand.class);
        }
        switch (command.getCommandType()) {
            case MAKE_MOVE -> {
            }
            case LEAVE -> {
                ctx.send(command.getUsername() + " left the game");
                ctx.closeSession();
            }
            case RESIGN -> {
                ctx.send(command.getUsername() + " has resigned");
            }
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        ctx.closeSession();
        System.out.println("Websocket closed!");
    }
}
