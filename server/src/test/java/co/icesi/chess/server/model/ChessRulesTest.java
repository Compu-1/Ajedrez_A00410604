package co.icesi.chess.server.model;
import co.icesi.chess.model.ChessRules;

import ChessApp.ChessPiece;
import ChessApp.PieceColor;
import ChessApp.PieceType;
import ChessApp.Position;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ChessRulesTest {

    @Test
    public void testInitialBoardSetup() {
        ChessPiece[][] board = ChessRules.createInitialBoard();
        assertEquals(8, board.length);
        assertEquals(8, board[0].length);

        // Blancas en fila 7 y peones en fila 6
        assertEquals(PieceType.PIECEROOK, board[7][0].type);
        assertEquals(PieceColor.COLORWHITE, board[7][0].color);
        assertEquals(PieceType.PIECEKING, board[7][4].type);
        assertEquals(PieceColor.COLORWHITE, board[7][4].color);
        assertEquals(PieceType.PIECEPAWN, board[6][4].type);

        // Negras en fila 0 y peones en fila 1
        assertEquals(PieceType.PIECEROOK, board[0][0].type);
        assertEquals(PieceColor.COLORBLACK, board[0][0].color);
        assertEquals(PieceType.PIECEKING, board[0][4].type);
        assertEquals(PieceColor.COLORBLACK, board[0][4].color);
        assertEquals(PieceType.PIECEPAWN, board[1][4].type);

        // Centro vacío
        assertTrue(ChessRules.isEmpty(board[3][3]));
        assertTrue(ChessRules.isEmpty(board[4][4]));

        // No hay jaque al inicio
        assertFalse(ChessRules.isKingInCheck(board, PieceColor.COLORWHITE));
        assertFalse(ChessRules.isKingInCheck(board, PieceColor.COLORBLACK));
    }

    @Test
    public void testWhitePawnInitialMoves() {
        ChessPiece[][] board = ChessRules.createInitialBoard();
        Position e2 = new Position(6, 4); // Peón de Rey e2

        List<Position> legal = ChessRules.getLegalMoves(board, e2);
        // Debe poder mover a e3 (5,4) o e4 (4,4)
        assertEquals(2, legal.size());

        boolean hasE3 = legal.stream().anyMatch(p -> p.row == 5 && p.col == 4);
        boolean hasE4 = legal.stream().anyMatch(p -> p.row == 4 && p.col == 4);
        assertTrue(hasE3);
        assertTrue(hasE4);
    }

    @Test
    public void testKnightJumps() {
        ChessPiece[][] board = ChessRules.createInitialBoard();
        Position b1 = new Position(7, 1); // Caballo b1

        List<Position> legal = ChessRules.getLegalMoves(board, b1);
        // Desde b1 puede saltar sobre peones a a3 (5,0) y c3 (5,2)
        assertEquals(2, legal.size());
        assertTrue(legal.stream().anyMatch(p -> p.row == 5 && p.col == 0));
        assertTrue(legal.stream().anyMatch(p -> p.row == 5 && p.col == 2));
    }

    @Test
    public void testPawnCapture() {
        ChessPiece[][] board = ChessRules.createInitialBoard();
        // Mover peón blanco a e4 (4,4) y peón negro a d5 (3,3)
        board[4][4] = ChessRules.createPiece(PieceType.PIECEPAWN, PieceColor.COLORWHITE);
        board[6][4] = ChessRules.createEmpty();
        board[3][3] = ChessRules.createPiece(PieceType.PIECEPAWN, PieceColor.COLORBLACK);
        board[1][3] = ChessRules.createEmpty();

        List<Position> legalWhite = ChessRules.getLegalMoves(board, new Position(4, 4));
        // Blanco puede avanzar a e5 (3,4) o capturar en d5 (3,3)
        assertTrue(legalWhite.stream().anyMatch(p -> p.row == 3 && p.col == 4)); // avance
        assertTrue(legalWhite.stream().anyMatch(p -> p.row == 3 && p.col == 3)); // captura
    }

    @Test
    public void testFoolsMateCheckmate() {
        // Mate del loco:
        // 1. f3 e5
        // 2. g4 Qh4# (Jaque mate a blancas)
        ChessPiece[][] board = ChessRules.createInitialBoard();

        // 1. f3: Peón f2 (6,5) a f3 (5,5)
        board = ChessRules.simulateMove(board, new Position(6, 5), new Position(5, 5));

        // 1... e5: Peón e7 (1,4) a e5 (3,4)
        board = ChessRules.simulateMove(board, new Position(1, 4), new Position(3, 4));

        // 2. g4: Peón g2 (6,6) a g4 (4,6)
        board = ChessRules.simulateMove(board, new Position(6, 6), new Position(4, 6));

        // 2... Qh4#: Dama negra d8 (0,3) a h4 (4,7)
        board = ChessRules.simulateMove(board, new Position(0, 3), new Position(4, 7));

        // Verificar que el Rey blanco (7,4) está en jaque
        assertTrue(ChessRules.isKingInCheck(board, PieceColor.COLORWHITE));

        // Verificar que las blancas no tienen ningún movimiento legal disponible -> Jaque Mate
        assertFalse(ChessRules.hasAnyLegalMove(board, PieceColor.COLORWHITE));
    }
}
