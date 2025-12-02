package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {

    private final ChessMove move;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, String username, ChessMove move) {
        super(commandType, authToken, gameID, username);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }
}
