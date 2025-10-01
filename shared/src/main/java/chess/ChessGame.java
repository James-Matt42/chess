package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor player;
    private Collection<ChessMove> moveHistory;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.player = TeamColor.WHITE;
        moveHistory = new HashSet<ChessMove>();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return player;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        player = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
//         Also make sure to add en passant and castling
        System.out.println(board);
        var piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        var pieceMoves = piece.pieceMoves(board, startPosition);
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            pieceMoves.addAll(addCastleMoves(piece.getTeamColor()));
        }
        return movesThatDoNotLeadToCheck(pieceMoves, piece.getTeamColor());
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
//        Check if there's a piece in the starting position
        var piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("Starting position is null");
        }

        //        Check if it's the player's turn
        var color = piece.getTeamColor();
        if (!color.equals(player)) {
            throw new InvalidMoveException(String.format("%s tried to move during %s's turn", color, player.toString()));
        }

//        Check if the move is valid
        Collection<ChessMove> valid = validMoves(move.getStartPosition());
        if (valid != null && valid.contains(move)) {
            board.makeMove(move);
            moveHistory.add(move);
            player = player == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
        } else {
            throw new InvalidMoveException(String.format("%s is not legal", move));
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckPrivate(teamColor, this.board);
    }

    private boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        return isInCheckPrivate(teamColor, board);
    }

    private boolean isInCheckPrivate(TeamColor teamColor, ChessBoard board) {
        TeamColor oppColor = teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
        var positions = board.getEndPositions(oppColor);
        var kingPosition = board.findKing(teamColor);

        for (var position : positions) {
            if (position.equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        var checkNow = isInCheck(teamColor);
        if (checkNow) {
            var colorMoves = board.getColorMoves(teamColor);
            var validMoves = movesThatDoNotLeadToCheck(colorMoves, teamColor);
            return validMoves.isEmpty();
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        boolean checkNow = isInCheck(teamColor);
        if (!checkNow) {
            var colorMoves = board.getColorMoves(teamColor);
            var validMoves = movesThatDoNotLeadToCheck(colorMoves, teamColor);
            return validMoves.isEmpty();
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && player == chessGame.player;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, player);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", player=" + player +
                '}';
    }

    private Collection<ChessMove> movesThatDoNotLeadToCheck(Collection<ChessMove> moves, TeamColor color) {
        var validMoves = new HashSet<ChessMove>();
        for (var move : moves) {
            var copyBoard = new ChessBoard(board.getBoardCopy());
            copyBoard.makeMove(move);
            if (!isInCheck(color, copyBoard)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    private Collection<ChessMove> addCastleMoves(TeamColor teamColor) {
        var castleMoves = new HashSet<ChessMove>();
        int row = teamColor == TeamColor.WHITE ? 1 : 8;
        var kingPosition = new ChessPosition(row, 5);
        if (!isInCheck(teamColor) && !isNull(kingPosition)) {
            var rookPositionLeft = new ChessPosition(row, 1);
            var rookPositionRight = new ChessPosition(row, 8);

//            Check if the king has moved
            if (!hasMovedFrom(kingPosition)) {
//                Check if either rook has moved
                if (!hasMovedFrom(rookPositionLeft) && !isNull(rookPositionLeft)) {
//                    Make sure that king doesn't pass through check
                    if (castleHelper(rookPositionLeft, kingPosition, teamColor, -1)) {
//                        Make sure that there's no piece next to the rook
                        if (board.getPiece(new ChessPosition(rookPositionLeft.getRow(), rookPositionLeft.getColumn() + 1)) == null) {
                            var endPos = new ChessPosition(kingPosition.getRow(), kingPosition.getColumn() - 2);
                            castleMoves.add(new ChessMove(kingPosition, endPos, null));
                        }
                    }
                }
                if (!hasMovedFrom(rookPositionRight) && !isNull(rookPositionRight)) {
                    if (castleHelper(rookPositionRight, kingPosition, teamColor, 1)) {
                        var endPos = new ChessPosition(kingPosition.getRow(), kingPosition.getColumn() + 2);
                        castleMoves.add(new ChessMove(kingPosition, endPos, null));
                    }
                }
            }
        }

        return castleMoves;
    }

    private boolean castleHelper(ChessPosition rookPosition, ChessPosition kingPosition, TeamColor teamColor, int rowOffset) {
        boolean add = true;
        if (!hasMovedFrom(rookPosition)) {
            ChessBoard copyBoard = new ChessBoard(board.getBoardCopy());
            var startPos = new ChessPosition(kingPosition.getRow(), kingPosition.getColumn());
            for (int i = 0; i < 2; i++) {
                var endPos = new ChessPosition(startPos.getRow(), startPos.getColumn() + rowOffset);
                add = add & board.getPiece(endPos) == null;
                copyBoard.makeMove(new ChessMove(kingPosition, endPos, null));
//                        if (isInCheck(teamColor, copyBoard)) {
//                            add = false;
//                        }
                add = add & !isInCheck(teamColor, copyBoard);
                startPos = new ChessPosition(endPos.getRow(), endPos.getColumn());
            }
        }
        return add;
    }

    private boolean isNull(ChessPosition position) {
        return board.getPiece(position) == null;
    }

    private boolean hasMovedFrom(ChessPosition position) {
        for (var move : moveHistory) {
            if (move.getStartPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }
}
