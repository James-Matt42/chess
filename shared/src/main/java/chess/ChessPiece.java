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

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
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

    private boolean validPosition(int row, int col) {
        return row > 0 && row < 9 && col > 0 && col < 9;
    }

    private boolean validPosition(ChessPosition position) {
        return position.getRow() > 0 && position.getRow() < 9 && position.getColumn() > 0 && position.getColumn() < 9;
    }

    private void addDiagonalMoves(HashSet<ChessMove> moves, int rowOffset, int colOffset, int rowLimit, ChessPosition myPosition, ChessPiece piece, ChessBoard board) {
        int col = myPosition.getColumn() + colOffset;
        for (int row = myPosition.getRow() + rowOffset; row != rowLimit; row += rowOffset) {
            ChessPosition pos = new ChessPosition(row, col);
            if (validPosition(pos)) {
                if (board.getPiece(pos) == null) {
                    moves.add(new ChessMove(myPosition, pos, null));
                } else {
                    if (board.getPiece(pos).getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, pos, null));
                    }
                    break;
                }
            } else {
                break;
            }
            col += colOffset;
        }
    }

    private Collection<ChessMove> bishopPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        var moves = new HashSet<ChessMove>();

        addDiagonalMoves(moves, 1, 1, 9, myPosition, piece, board);
        addDiagonalMoves(moves, 1, -1, 9, myPosition, piece, board);
        addDiagonalMoves(moves, -1, 1, 0, myPosition, piece, board);
        addDiagonalMoves(moves, -1, -1, 0, myPosition, piece, board);

        return moves;
    }

    private Collection<ChessMove> kingPieceMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }

    private Collection<ChessMove> knightPieceMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }

    private Collection<ChessMove> pawnPieceMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }

    private Collection<ChessMove> queenPieceMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }

    private Collection<ChessMove> rookPieceMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }
}
