package client;

import jakarta.websocket.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class WsClient extends Endpoint {

    public Session session;
    private final String username;

    public WsClient(int port, String username) throws Exception {
        this.username = username;
        URI uri = new URI(String.format("ws://localhost:%d/ws", port));
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                parseMessage(message);
            }
        });
    }

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void close() throws IOException {
        session.close();
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    private void parseMessage(String message) {
        System.out.println("\n" + message);
        System.out.print("[GAME] >> ");

    }
}
