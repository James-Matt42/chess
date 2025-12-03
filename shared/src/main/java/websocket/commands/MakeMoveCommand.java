package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {

    private final String username;
    private final ChessMove move;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, String username, ChessMove move) {
        super(commandType, authToken, gameID);
        this.username = username;
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }

    public Object getUsername() {
        return username;
    }
}
