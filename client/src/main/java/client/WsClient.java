package client;

import chess.ChessGame;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.messages.LoadBoardMessage;
import websocket.messages.ServerErrorMessage;
import websocket.messages.ServerNotificationMessage;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

public class WsClient extends Endpoint {

    public Session session;
    private final ChessGame.TeamColor playerColor;
    private ChessGame game;

    public WsClient(int port, ChessGame.TeamColor teamColor) throws Exception {
        this.playerColor = teamColor;
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
        var gson = new Gson();
        HashMap<String, String> map = new Gson().fromJson(message, HashMap.class);
        if (map.get("serverMessageType").equals("LOAD_GAME")) {
            LoadBoardMessage loadBoardMessage = gson.fromJson(message, LoadBoardMessage.class);
            this.game = loadBoardMessage.getGame();
            System.out.println("\n");
            DrawBoard.drawBoard(game.getBoard(), playerColor);
            System.out.print("[GAME] >> ");
        } else if (map.get("serverMessageType").equals("NOTIFICATION")) {
            ServerNotificationMessage command = gson.fromJson(message, ServerNotificationMessage.class);
            System.out.print("\n" + command.getMessage() + "\n[GAME] >> ");
        } else if (map.get("serverMessageType").equals("ERROR")) {
            ServerErrorMessage command = gson.fromJson(message, ServerErrorMessage.class);
            System.out.print("\n" + command.getErrorMessage() + "\n[GAME] >> ");
        }
    }

    public ChessGame getGame() {
        return game;
    }
}
