package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private final int RECURSE_LIMIT;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        RECURSE_LIMIT = 7;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        var piece = board.getPiece(myPosition);
        switch (piece.getPieceType()) {
            case BISHOP -> {
                return bishopPieceMoves(board, myPosition);
            }
            case KING -> {
                return kingPieceMoves(board, myPosition);
            }
            case KNIGHT -> {
                return knightPieceMoves(board, myPosition);
            }
            case PAWN -> {
                return pawnPieceMoves(board, myPosition);
            }
            case QUEEN -> {
                return queenPieceMoves(board, myPosition);
            }
            case ROOK -> {
                return rookPieceMoves(board, myPosition);
            }
        }
        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    private void addMove(ChessPosition start, int rowOffset, int colOffset, int numRepeats, ChessPiece piece, ChessBoard board, HashSet<ChessMove> moves) {
        int rowOffset_copy = rowOffset;
        int colOffset_copy = colOffset;
        for (int i = 0; i < numRepeats; i++) {
            ChessPosition pos = new ChessPosition(start.getRow() + rowOffset_copy, start.getColumn() + colOffset_copy);
            if (validPosition(pos)) {
                if (board.getPiece(pos) == null) {
                    moves.add(new ChessMove(start, pos, null));
                } else if (board.getPiece(pos).getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(start, pos, null));
                    return;
                } else {
                    return;
                }
            } else {
                return;
            }
            rowOffset_copy += rowOffset;
            colOffset_copy += colOffset;
        }
    }

    private boolean validPosition(ChessPosition position) {
        return position.getRow() > 0 && position.getRow() < 9 && position.getColumn() > 0 && position.getColumn() < 9;
    }

    private void addDiagonal(ChessPosition start, int numRepeats, ChessPiece piece, ChessBoard board, HashSet<ChessMove> moves) {
        addMove(start, 1, 1, numRepeats, piece, board, moves);
        addMove(start, 1, -1, numRepeats, piece, board, moves);
        addMove(start, -1, 1, numRepeats, piece, board, moves);
        addMove(start, -1, -1, numRepeats, piece, board, moves);
    }

    private void addRowCol(ChessPosition start, int numRepeats, ChessPiece piece, ChessBoard board, HashSet<ChessMove> moves) {
        addMove(start, 1, 0, numRepeats, piece, board, moves);
        addMove(start, -1, 0, numRepeats, piece, board, moves);
        addMove(start, 0, 1, numRepeats, piece, board, moves);
        addMove(start, 0, -1, numRepeats, piece, board, moves);
    }

    private Collection<ChessMove> bishopPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        var moves = new HashSet<ChessMove>();

        addDiagonal(myPosition, RECURSE_LIMIT, piece, board, moves);
        return moves;
    }

    private Collection<ChessMove> kingPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        var moves = new HashSet<ChessMove>();

        addDiagonal(myPosition, 1, piece, board, moves);
        addRowCol(myPosition, 1, piece, board, moves);

        return moves;
    }

    private Collection<ChessMove> knightPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        var moves = new HashSet<ChessMove>();

        addMove(myPosition, 2, -1, 1, piece, board, moves);
        addMove(myPosition, 2, 1, 1, piece, board, moves);
        addMove(myPosition, 1, 2, 1, piece, board, moves);
        addMove(myPosition, -1, 2, 1, piece, board, moves);
        addMove(myPosition, -2, 1, 1, piece, board, moves);
        addMove(myPosition, -2, -1, 1, piece, board, moves);
        addMove(myPosition, -1, -2, 1, piece, board, moves);
        addMove(myPosition, 1, -2, 1, piece, board, moves);

        return moves;
    }

    private Collection<ChessMove> pawnPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        var moves = new HashSet<ChessMove>();

        int rowOffset;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            rowOffset = 1;
        } else {
            rowOffset = -1;
        }

//        Validate normal positions
        for (int new_col = col - 1; new_col < col + 2; new_col++) {
            var pos = new ChessPosition(row + rowOffset, new_col);
//            validateAndAdd(pos, board, piece, myPosition, moves);
            if ((validPosition(pos) && new_col == col && board.getPiece(pos) == null) || (validPosition(pos) && new_col != col && board.getPiece(pos) != null && board.getPiece(pos).getTeamColor() != piece.getTeamColor())) {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && row + rowOffset == 8 || piece.getTeamColor() == ChessGame.TeamColor.BLACK && row + rowOffset == 1) {
                    moves.add(new ChessMove(myPosition, pos, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, pos, PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, pos, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, pos, PieceType.KNIGHT));
                } else {
                    moves.add(new ChessMove(myPosition, pos, null));
                }
            }
        }
//        Validate beginning positions
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2 || piece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7) {
            var pos = new ChessPosition(row + (2 * rowOffset), col);
            if (validPosition(pos) && board.getPiece(pos) == null && board.getPiece(new ChessPosition(row + rowOffset, col)) == null) {
                moves.add(new ChessMove(myPosition, pos, null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> queenPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        var moves = new HashSet<ChessMove>();

        addRowCol(myPosition, RECURSE_LIMIT, piece, board, moves);
        addDiagonal(myPosition, RECURSE_LIMIT, piece, board, moves);

        return moves;
    }

    private Collection<ChessMove> rookPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        var moves = new HashSet<ChessMove>();

        addRowCol(myPosition, RECURSE_LIMIT, piece, board, moves);

        return moves;
    }
}
