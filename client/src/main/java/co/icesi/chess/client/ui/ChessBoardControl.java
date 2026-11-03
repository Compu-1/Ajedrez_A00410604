package co.icesi.chess.client.ui;

import ChessApp.*;
import co.icesi.chess.model.ChessRules;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ChessBoardControl extends StackPane {

    public static final int TILE_SIZE = 64;
    private static final String LIGHT_COLOR = "#EEEED2";
    private static final String DARK_COLOR = "#769656";
    private static final String SELECTED_COLOR = "#F7F769";
    private static final String LAST_MOVE_COLOR = "#CED964";
    private static final String PIECE_FONT_FAMILY = "-fx-font-family: 'Segoe UI Symbol', 'DejaVu Sans', 'Noto Sans Symbols', 'FreeSerif', 'Arial Unicode MS', serif;";

    private final GridPane grid = new GridPane();
    private final SquarePane[][] squares = new SquarePane[8][8];

    private ChessPiece[][] currentBoard;
    private PieceColor myColor = PieceColor.COLORNONE;
    private boolean isMyTurn = false;
    private boolean isSpectator = false;

    private Position selectedPos = null;
    private List<Position> highlightedLegalMoves = new ArrayList<>();
    private Position lastMoveFrom = null;
    private Position lastMoveTo = null;

    private BiConsumer<Position, Position> moveListener;

    public ChessBoardControl() {
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-border-color: #3b4d2b; -fx-border-width: 4px; -fx-border-radius: 4px; -fx-background-color: #2b3920;");

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                SquarePane square = new SquarePane(r, c);
                squares[r][c] = square;
                final int row = r;
                final int col = c;
                square.setOnMouseClicked(e -> handleSquareClick(row, col));
                grid.add(square, c, r);
            }
        }

        getChildren().add(grid);
        this.currentBoard = ChessRules.createInitialBoard();
        render();
    }

    public void setMoveListener(BiConsumer<Position, Position> listener) {
        this.moveListener = listener;
    }

    public void updateState(GameState state, PieceColor playerColor, boolean spectator) {
        this.currentBoard = state.board;
        this.myColor = playerColor;
        this.isSpectator = spectator;
        this.isMyTurn = !spectator && (state.currentTurn == playerColor);

        // Detectar último movimiento para resaltado visual
        if (state.moves != null && state.moves.length > 0) {
            MoveRecord last = state.moves[state.moves.length - 1];
            this.lastMoveFrom = last.fromPos;
            this.lastMoveTo = last.toPos;
        } else {
            this.lastMoveFrom = null;
            this.lastMoveTo = null;
        }

        // Si cambió el turno o el estado, limpiar selección previa
        this.selectedPos = null;
        this.highlightedLegalMoves.clear();

        render();
    }

    private void handleSquareClick(int r, int c) {
        if (isSpectator || !isMyTurn || currentBoard == null) {
            return;
        }

        Position clickedPos = new Position(r, c);
        ChessPiece clickedPiece = currentBoard[r][c];

        // 1. Si ya teníamos una pieza seleccionada y hacemos clic en un movimiento legal
        if (selectedPos != null) {
            boolean isTargetLegal = highlightedLegalMoves.stream().anyMatch(p -> p.row == r && p.col == c);
            if (isTargetLegal) {
                Position from = selectedPos;
                selectedPos = null;
                highlightedLegalMoves.clear();
                render();
                if (moveListener != null) {
                    moveListener.accept(from, clickedPos);
                }
                return;
            }
        }

        // 2. Si hacemos clic en una pieza propia, la seleccionamos
        if (!ChessRules.isEmpty(clickedPiece) && clickedPiece.color == myColor) {
            selectedPos = clickedPos;
            highlightedLegalMoves = ChessRules.getLegalMoves(currentBoard, clickedPos);
            render();
            return;
        }

        // 3. Clic en casilla vacía o inválida: deseleccionar
        selectedPos = null;
        highlightedLegalMoves.clear();
        render();
    }

    public void render() {
        if (currentBoard == null) return;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                SquarePane square = squares[r][c];
                boolean isLight = (r + c) % 2 == 0;
                String baseColor = isLight ? LIGHT_COLOR : DARK_COLOR;

                // Color por último movimiento
                if ((lastMoveFrom != null && lastMoveFrom.row == r && lastMoveFrom.col == c) ||
                    (lastMoveTo != null && lastMoveTo.row == r && lastMoveTo.col == c)) {
                    baseColor = LAST_MOVE_COLOR;
                }

                // Color si está seleccionada
                if (selectedPos != null && selectedPos.row == r && selectedPos.col == c) {
                    baseColor = SELECTED_COLOR;
                }

                square.setBackgroundColor(baseColor);

                // Renderizar pieza
                ChessPiece piece = currentBoard[r][c];
                square.setPiece(piece);

                // Marcar sugerencia de movimiento legal
                final int curR = r;
                final int curC = c;
                boolean isLegalTarget = highlightedLegalMoves.stream().anyMatch(p -> p.row == curR && p.col == curC);
                boolean isCapture = isLegalTarget && !ChessRules.isEmpty(piece);
                square.showHint(isLegalTarget, isCapture);
            }
        }
    }

    private static class SquarePane extends StackPane {
        private final int row;
        private final int col;
        private final Label pieceLabel = new Label();
        private final Circle moveDot = new Circle(8, Color.web("#4CAF50", 0.75));
        private final Circle captureRing = new Circle(26, Color.TRANSPARENT);

        public SquarePane(int row, int col) {
            this.row = row;
            this.col = col;
            setPrefSize(TILE_SIZE, TILE_SIZE);
            setMinSize(TILE_SIZE, TILE_SIZE);
            setMaxSize(TILE_SIZE, TILE_SIZE);

            pieceLabel.setStyle(PIECE_FONT_FAMILY + " -fx-font-size: 38px; -fx-alignment: center;");
            captureRing.setStroke(Color.web("#E53935", 0.85));
            captureRing.setStrokeWidth(4);

            moveDot.setVisible(false);
            captureRing.setVisible(false);

            getChildren().addAll(pieceLabel, moveDot, captureRing);
        }

        public void setBackgroundColor(String color) {
            setStyle(String.format("-fx-background-color: %s;", color));
        }

        public void setPiece(ChessPiece piece) {
            if (piece == null || piece.type == PieceType.PIECEEMPTY || piece.color == PieceColor.COLORNONE) {
                pieceLabel.setText("");
                return;
            }

            String symbol = getUnicodeSymbol(piece.type, piece.color);
            pieceLabel.setText(symbol);

            if (piece.color == PieceColor.COLORWHITE) {
                pieceLabel.setStyle(PIECE_FONT_FAMILY + " -fx-font-size: 40px; -fx-text-fill: #FFFFFF; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 4, 0.6, 0, 1);");
            } else {
                pieceLabel.setStyle(PIECE_FONT_FAMILY + " -fx-font-size: 40px; -fx-text-fill: #1A1A1A; -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.45), 2, 0.3, 0, 0);");
            }
        }

        public void showHint(boolean isLegal, boolean isCapture) {
            if (!isLegal) {
                moveDot.setVisible(false);
                captureRing.setVisible(false);
            } else if (isCapture) {
                moveDot.setVisible(false);
                captureRing.setVisible(true);
            } else {
                moveDot.setVisible(true);
                captureRing.setVisible(false);
            }
        }

        private static String getUnicodeSymbol(PieceType type, PieceColor color) {
            // Se usan los glifos sólidos (\u265A-\u265F) coloreados por CSS para máxima legibilidad en casillas claras y oscuras
            switch (type) {
                case PIECEKING:   return "\u265A";
                case PIECEQUEEN:  return "\u265B";
                case PIECEROOK:   return "\u265C";
                case PIECEBISHOP: return "\u265D";
                case PIECEKNIGHT: return "\u265E";
                case PIECEPAWN:   return "\u265F";
                default: return "";
            }
        }
    }
}
