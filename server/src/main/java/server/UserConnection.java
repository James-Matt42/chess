package server;

import io.javalin.websocket.WsMessageContext;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.ParticipationType;

public record UserConnection(WsMessageContext ctx, ParticipationType participationType) {
}
