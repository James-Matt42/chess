package server;

import io.javalin.websocket.WsMessageContext;
import websocket.commands.ParticipationType;

public record UserConnection(WsMessageContext ctx, ParticipationType participationType) {
}
