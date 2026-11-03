package co.icesi.chess.model;

import ChessApp.ChessPiece;
import ChessApp.PieceColor;
import ChessApp.PieceType;
import ChessApp.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Motor de reglas de ajedrez completo y determinista.
 * Implementa movimiento de piezas, capturas, coronación automática a Reina,
 * validación de jaque, cálculo de movimientos legales, y detección de jaque mate / tablas.
 */
public class ChessRules {

    public static final int BOARD_SIZE = 8;

    public static ChessPiece createPiece(PieceType type, PieceColor color) {
        return new ChessPiece(type, color);
    }

    public static ChessPiece createEmpty() {
        return new ChessPiece(PieceType.PIECEEMPTY, PieceColor.COLORNONE);
    }

    public static ChessPiece[][] createInitialBoard() {
        ChessPiece[][] board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];

        // Fila 0: Piezas mayores Negras
        board[0][0] = createPiece(PieceType.PIECEROOK, PieceColor.COLORBLACK);
        board[0][1] = createPiece(PieceType.PIECEKNIGHT, PieceColor.COLORBLACK);
        board[0][2] = createPiece(PieceType.PIECEBISHOP, PieceColor.COLORBLACK);
        board[0][3] = createPiece(PieceType.PIECEQUEEN, PieceColor.COLORBLACK);
        board[0][4] = createPiece(PieceType.PIECEKING, PieceColor.COLORBLACK);
        board[0][5] = createPiece(PieceType.PIECEBISHOP, PieceColor.COLORBLACK);
        board[0][6] = createPiece(PieceType.PIECEKNIGHT, PieceColor.COLORBLACK);
        board[0][7] = createPiece(PieceType.PIECEROOK, PieceColor.COLORBLACK);

        // Fila 1: Peones Negros
        for (int c = 0; c < BOARD_SIZE; c++) {
            board[1][c] = createPiece(PieceType.PIECEPAWN, PieceColor.COLORBLACK);
        }

        // Filas 2 a 5: Casillas vacías
        for (int r = 2; r <= 5; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c] = createEmpty();
            }
        }

        // Fila 6: Peones Blancos
        for (int c = 0; c < BOARD_SIZE; c++) {
            board[6][c] = createPiece(PieceType.PIECEPAWN, PieceColor.COLORWHITE);
        }

        // Fila 7: Piezas mayores Blancas
        board[7][0] = createPiece(PieceType.PIECEROOK, PieceColor.COLORWHITE);
        board[7][1] = createPiece(PieceType.PIECEKNIGHT, PieceColor.COLORWHITE);
        board[7][2] = createPiece(PieceType.PIECEBISHOP, PieceColor.COLORWHITE);
        board[7][3] = createPiece(PieceType.PIECEQUEEN, PieceColor.COLORWHITE);
        board[7][4] = createPiece(PieceType.PIECEKING, PieceColor.COLORWHITE);
        board[7][5] = createPiece(PieceType.PIECEBISHOP, PieceColor.COLORWHITE);
        board[7][6] = createPiece(PieceType.PIECEKNIGHT, PieceColor.COLORWHITE);
        board[7][7] = createPiece(PieceType.PIECEROOK, PieceColor.COLORWHITE);

        return board;
    }

    public static ChessPiece[][] cloneBoard(ChessPiece[][] original) {
        ChessPiece[][] copy = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                ChessPiece p = original[r][c];
                copy[r][c] = new ChessPiece(p.type, p.color);
            }
        }
        return copy;
    }

    public static boolean isInside(int r, int c) {
        return r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE;
    }

    public static boolean isEmpty(ChessPiece piece) {
        return piece == null || piece.type == PieceType.PIECEEMPTY || piece.color == PieceColor.COLORNONE;
    }

    public static PieceColor opponentColor(PieceColor color) {
        if (color == PieceColor.COLORWHITE) return PieceColor.COLORBLACK;
        if (color == PieceColor.COLORBLACK) return PieceColor.COLORWHITE;
        return PieceColor.COLORNONE;
    }

    public static Position findKing(ChessPiece[][] board, PieceColor kingColor) {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                ChessPiece p = board[r][c];
                if (p.type == PieceType.PIECEKING && p.color == kingColor) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }

    /**
     * Determina si la casilla (targetR, targetC) está bajo ataque de alguna pieza de attackerColor.
     */
    public static boolean isSquareAttacked(ChessPiece[][] board, int targetR, int targetC, PieceColor attackerColor) {
        // 1. Ataque de Peones
        int pawnProw = (attackerColor == PieceColor.COLORWHITE) ? targetR + 1 : targetR - 1;
        if (pawnProw >= 0 && pawnProw < BOARD_SIZE) {
            for (int dc : new int[]{-1, 1}) {
                int pcol = targetC + dc;
                if (pcol >= 0 && pcol < BOARD_SIZE) {
                    ChessPiece p = board[pawnProw][pcol];
                    if (p.type == PieceType.PIECEPAWN && p.color == attackerColor) {
                        return true;
                    }
                }
            }
        }

        // 2. Ataque de Caballos
        int[][] knightDeltas = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };
        for (int[] d : knightDeltas) {
            int nr = targetR + d[0];
            int nc = targetC + d[1];
            if (isInside(nr, nc)) {
                ChessPiece p = board[nr][nc];
                if (p.type == PieceType.PIECEKNIGHT && p.color == attackerColor) {
                    return true;
                }
            }
        }

        // 3. Ataque de Rey (1 casilla)
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = targetR + dr;
                int nc = targetC + dc;
                if (isInside(nr, nc)) {
                    ChessPiece p = board[nr][nc];
                    if (p.type == PieceType.PIECEKING && p.color == attackerColor) {
                        return true;
                    }
                }
            }
        }

        // 4. Rayos lineales (Torre o Reina)
        int[][] rookDirections = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : rookDirections) {
            int nr = targetR + d[0];
            int nc = targetC + d[1];
            while (isInside(nr, nc)) {
                ChessPiece p = board[nr][nc];
                if (!isEmpty(p)) {
                    if (p.color == attackerColor && (p.type == PieceType.PIECEROOK || p.type == PieceType.PIECEQUEEN)) {
                        return true;
                    }
                    break;
                }
                nr += d[0];
                nc += d[1];
            }
        }

        // 5. Rayos diagonales (Alfil o Reina)
        int[][] bishopDirections = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] d : bishopDirections) {
            int nr = targetR + d[0];
            int nc = targetC + d[1];
            while (isInside(nr, nc)) {
                ChessPiece p = board[nr][nc];
                if (!isEmpty(p)) {
                    if (p.color == attackerColor && (p.type == PieceType.PIECEBISHOP || p.type == PieceType.PIECEQUEEN)) {
                        return true;
                    }
                    break;
                }
                nr += d[0];
                nc += d[1];
            }
        }

        return false;
    }

    /**
     * Valida si el Rey de kingColor se encuentra en jaque en el tablero dado.
     */
    public static boolean isKingInCheck(ChessPiece[][] board, PieceColor kingColor) {
        Position kingPos = findKing(board, kingColor);
        if (kingPos == null) return false;
        PieceColor enemy = opponentColor(kingColor);
        return isSquareAttacked(board, kingPos.row, kingPos.col, enemy);
    }

    /**
     * Obtiene los movimientos pseudo-legales de una pieza sin filtrar aún por jaque al propio Rey.
     */
    public static List<Position> getPseudoLegalMoves(ChessPiece[][] board, Position from) {
        List<Position> moves = new ArrayList<>();
        if (!isInside(from.row, from.col)) return moves;

        ChessPiece piece = board[from.row][from.col];
        if (isEmpty(piece)) return moves;

        PieceColor myColor = piece.color;
        PieceColor enemyColor = opponentColor(myColor);

        switch (piece.type) {
            case PIECEPAWN: {
                int forwardDir = (myColor == PieceColor.COLORWHITE) ? -1 : 1;
                int startRow = (myColor == PieceColor.COLORWHITE) ? 6 : 1;

                // Avance 1 casilla
                int oneStepR = from.row + forwardDir;
                if (isInside(oneStepR, from.col) && isEmpty(board[oneStepR][from.col])) {
                    moves.add(new Position(oneStepR, from.col));

                    // Avance 2 casillas desde posición inicial
                    int twoStepR = from.row + 2 * forwardDir;
                    if (from.row == startRow && isInside(twoStepR, from.col) && isEmpty(board[twoStepR][from.col])) {
                        moves.add(new Position(twoStepR, from.col));
                    }
                }

                // Capturas diagonales
                for (int dc : new int[]{-1, 1}) {
                    int capC = from.col + dc;
                    if (isInside(oneStepR, capC)) {
                        ChessPiece target = board[oneStepR][capC];
                        if (!isEmpty(target) && target.color == enemyColor) {
                            moves.add(new Position(oneStepR, capC));
                        }
                    }
                }
                break;
            }

            case PIECEKNIGHT: {
                int[][] deltas = {
                    {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                    {1, -2}, {1, 2}, {2, -1}, {2, 1}
                };
                for (int[] d : deltas) {
                    int nr = from.row + d[0];
                    int nc = from.col + d[1];
                    if (isInside(nr, nc)) {
                        ChessPiece target = board[nr][nc];
                        if (isEmpty(target) || target.color == enemyColor) {
                            moves.add(new Position(nr, nc));
                        }
                    }
                }
                break;
            }

            case PIECEBISHOP: {
                int[][] dirs = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                addRayMoves(board, from, dirs, enemyColor, moves);
                break;
            }

            case PIECEROOK: {
                int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                addRayMoves(board, from, dirs, enemyColor, moves);
                break;
            }

            case PIECEQUEEN: {
                int[][] dirs = {
                    {-1, -1}, {-1, 1}, {1, -1}, {1, 1},
                    {-1, 0}, {1, 0}, {0, -1}, {0, 1}
                };
                addRayMoves(board, from, dirs, enemyColor, moves);
                break;
            }

            case PIECEKING: {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = from.row + dr;
                        int nc = from.col + dc;
                        if (isInside(nr, nc)) {
                            ChessPiece target = board[nr][nc];
                            if (isEmpty(target) || target.color == enemyColor) {
                                moves.add(new Position(nr, nc));
                            }
                        }
                    }
                }
                break;
            }

            default:
                break;
        }

        return moves;
    }

    private static void addRayMoves(ChessPiece[][] board, Position from, int[][] dirs, PieceColor enemyColor, List<Position> moves) {
        for (int[] d : dirs) {
            int nr = from.row + d[0];
            int nc = from.col + d[1];
            while (isInside(nr, nc)) {
                ChessPiece target = board[nr][nc];
                if (isEmpty(target)) {
                    moves.add(new Position(nr, nc));
                } else {
                    if (target.color == enemyColor) {
                        moves.add(new Position(nr, nc));
                    }
                    break;
                }
                nr += d[0];
                nc += d[1];
            }
        }
    }

    /**
     * Aplica un movimiento de simulación sobre una copia del tablero.
     * Si un peón llega a la última fila, se promueve automáticamente a Reina.
     */
    public static ChessPiece[][] simulateMove(ChessPiece[][] board, Position from, Position to) {
        ChessPiece[][] next = cloneBoard(board);
        ChessPiece moving = next[from.row][from.col];

        // Promoción automática de peón a reina
        if (moving.type == PieceType.PIECEPAWN) {
            if ((moving.color == PieceColor.COLORWHITE && to.row == 0) ||
                (moving.color == PieceColor.COLORBLACK && to.row == 7)) {
                moving = createPiece(PieceType.PIECEQUEEN, moving.color);
            }
        }

        next[to.row][to.col] = moving;
        next[from.row][from.col] = createEmpty();
        return next;
    }

    /**
     * Obtiene únicamente los movimientos legales (descartando aquellos que dejan al propio Rey en jaque).
     */
    public static List<Position> getLegalMoves(ChessPiece[][] board, Position from) {
        List<Position> legal = new ArrayList<>();
        ChessPiece piece = board[from.row][from.col];
        if (isEmpty(piece)) return legal;

        List<Position> candidates = getPseudoLegalMoves(board, from);
        for (Position to : candidates) {
            ChessPiece[][] sim = simulateMove(board, from, to);
            if (!isKingInCheck(sim, piece.color)) {
                legal.add(to);
            }
        }
        return legal;
    }

    /**
     * Determina si el jugador tiene al menos un movimiento legal en todo el tablero.
     */
    public static boolean hasAnyLegalMove(ChessPiece[][] board, PieceColor color) {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                ChessPiece p = board[r][c];
                if (p.color == color) {
                    List<Position> legal = getLegalMoves(board, new Position(r, c));
                    if (!legal.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Convierte la posición (row, col) a notación algebraica clásica (ej. e4, d5, a8).
     */
    public static String toAlgebraicSquare(Position pos) {
        char colChar = (char) ('a' + pos.col);
        int rankNum = 8 - pos.row;
        return "" + colChar + rankNum;
    }

    /**
     * Genera la notación de un movimiento ejecutado (ej. e4, Nf3, Bxc6, Qh5#).
     */
    public static String formatMoveNotation(ChessPiece pieceBefore, Position from, Position to, boolean isCapture, boolean isCheck, boolean isCheckmate) {
        StringBuilder sb = new StringBuilder();
        if (pieceBefore.type == PieceType.PIECEPAWN) {
            if (isCapture) {
                sb.append((char) ('a' + from.col));
                sb.append('x');
            }
            sb.append(toAlgebraicSquare(to));
            if ((pieceBefore.color == PieceColor.COLORWHITE && to.row == 0) ||
                (pieceBefore.color == PieceColor.COLORBLACK && to.row == 7)) {
                sb.append("=Q");
            }
        } else {
            switch (pieceBefore.type) {
                case PIECEKNIGHT: sb.append('N'); break;
                case PIECEBISHOP: sb.append('B'); break;
                case PIECEROOK:   sb.append('R'); break;
                case PIECEQUEEN:  sb.append('Q'); break;
                case PIECEKING:   sb.append('K'); break;
                default: break;
            }
            if (isCapture) {
                sb.append('x');
            }
            sb.append(toAlgebraicSquare(to));
        }

        if (isCheckmate) {
            sb.append('#');
        } else if (isCheck) {
            sb.append('+');
        }

        return sb.toString();
    }
}
