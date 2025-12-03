package server;

import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.ParticipationType;

public record UserConnection(String sessionId, Session session, ParticipationType participationType) {
}
