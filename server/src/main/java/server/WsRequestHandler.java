package server;

import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;

public class WsRequestHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) throws Exception {
        ctx.enableAutomaticPings();
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
        return "";
    }
}
