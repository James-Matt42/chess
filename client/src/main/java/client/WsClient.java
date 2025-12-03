package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPosition;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.messages.LoadBoardMessage;
import websocket.messages.ServerMessage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class WsClient extends Endpoint {

    public Session session;
    private final String username;
    private ChessGame.TeamColor playerColor;
    private ChessBoard board;

    public WsClient(int port, String username, ChessGame.TeamColor teamColor) throws Exception {
        this.username = username;
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
        ServerMessage command = gson.fromJson(message, ServerMessage.class);
        if (command.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            LoadBoardMessage loadBoardMessage = gson.fromJson(message, LoadBoardMessage.class);
            this.board = loadBoardMessage.getGame();
            System.out.println("\n");
            DrawBoard.drawBoard(board, playerColor);
            System.out.print("[GAME] >> ");
        } else {
            String messageType;
            if (command.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
                messageType = "Notification";
            } else {
                messageType = "Error";
            }
            System.out.print("\n" + messageType + ": " + command.getMessage() + "\n[GAME] >> ");
        }
    }

    public ChessBoard getBoard() {
        return board;
    }
}
