package server;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;

public class WsRequestHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) throws Exception {
        ctx.enableAutomaticPings();
        ctx.send("This is a test");
        System.out.println("Websocket connected!");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        ctx.send(parseMessage(ctx));
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        System.out.println("Websocket closed!");
    }

    private String parseMessage(WsMessageContext ctx) {
//        Determine what kind of message is being sent
        var message = ctx.message();
        var command = new Gson().fromJson(message, UserGameCommand.class);
        return "Another test";
    }
}
