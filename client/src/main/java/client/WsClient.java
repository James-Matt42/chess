package client;

import jakarta.websocket.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class WsClient extends Endpoint {

    public Session session;

    public WsClient(int port) throws Exception {
        URI uri = new URI(String.format("ws://localhost:%d/ws", port));
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
            }
        });
    }

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
