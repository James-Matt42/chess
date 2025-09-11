package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private final ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
//        Takes in position as 1-based index and converts it to 0-based index on the squares board
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        setBackRow(ChessGame.TeamColor.WHITE);
        setBackRow(ChessGame.TeamColor.BLACK);
        setPawns(ChessGame.TeamColor.WHITE);
        setPawns(ChessGame.TeamColor.BLACK);
        clearMiddleSpace();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();
        boardString.append("[");
        for (int row = 1; row < 9; row++) {
            boardString.append("[");
            for (int col = 1; col < 9; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                if (getPiece(pos) == null) {
                    continue;
                }
                boardString.append(getPiece(pos).toString());
                if (col < 8) {
                    boardString.append(", ");
                }
            }
            boardString.append("]");
            if (row < 8) {
                boardString.append(", ");
            }
        }
        boardString.append("]");

        return boardString.toString();
    }

    private void setBackRow(ChessGame.TeamColor color) {
        int row;
        if (color == ChessGame.TeamColor.WHITE) {
            row = 0;
        } else {
            row = 7;
        }
        board[row][0] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        board[row][1] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        board[row][2] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        board[row][3] = new ChessPiece(color, ChessPiece.PieceType.QUEEN);
        board[row][4] = new ChessPiece(color, ChessPiece.PieceType.KING);
        board[row][5] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        board[row][6] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        board[row][7] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
    }

    private void setPawns(ChessGame.TeamColor color) {
        int row;
        if (color == ChessGame.TeamColor.WHITE) {
            row = 1;
        } else {
            row = 6;
        }

        for (int i = 0; i < 8; i++) {
            board[row][i] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
        }
    }

    private void clearMiddleSpace() {
        for (int row = 2; row < 6; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        }
    }
}
