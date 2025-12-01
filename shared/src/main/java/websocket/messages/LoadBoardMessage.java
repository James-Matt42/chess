package websocket.messages;

import chess.ChessBoard;
import chess.ChessMove;

public class LoadBoardMessage extends ServerMessage {

    final private ChessBoard game;

    public LoadBoardMessage(ServerMessageType type, ChessBoard game) {
        super(type);
        this.game = game;
    }

    public ChessBoard getGame() {
        return game;
    }
}
