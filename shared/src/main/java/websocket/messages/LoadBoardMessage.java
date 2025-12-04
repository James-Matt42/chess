package websocket.messages;

import chess.ChessGame;

public class LoadBoardMessage extends ServerMessage {

    final private ChessGame game;

    public LoadBoardMessage(ServerMessageType type, ChessGame game) {
        super(type);
        this.game = game;
    }

    public ChessGame getGame() {
        return game;
    }
}
