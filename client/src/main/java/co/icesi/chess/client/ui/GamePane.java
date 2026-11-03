package co.icesi.chess.client.ui;

import ChessApp.*;
import co.icesi.chess.client.network.ChessClientService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;

public class GamePane extends BorderPane {

    private static final String FONT_FAMILY = "-fx-font-family: 'Segoe UI Symbol', 'Segoe UI', 'DejaVu Sans', 'Arial Unicode MS', sans-serif;";

    private final ChessClientService clientService;
    private final Runnable onLeaveLobbyRequested;

    private final ChessBoardControl boardControl = new ChessBoardControl();

    // Elementos de la barra superior
    private final Label roomCodeLabel = new Label("Sala: ----");
    private final Button copyCodeBtn = new Button("Copiar c\u00F3digo");
    private final Label roleBadge = new Label("Rol: -");
    private final Label turnStatusLabel = new Label("Esperando jugadores...");
    private final Button resignBtn = new Button("Rendirse");
    private final Button leaveBtn = new Button("Salir");

    // Elementos del panel lateral derecho (Jugadores y movimientos)
    private final Label topPlayerName = new Label("-");
    private final HBox topPlayerTierBox = new HBox();
    private final Label topPlayerPoints = new Label("1200 pts");
    private final Label topCapturedLabel = new Label("");

    private final Label bottomPlayerName = new Label("-");
    private final HBox bottomPlayerTierBox = new HBox();
    private final Label bottomPlayerPoints = new Label("1200 pts");
    private final Label bottomCapturedLabel = new Label("");

    private final Label spectatorsCountLabel = new Label("Espectadores: 0");
    private final ListView<String> moveHistoryList = new ListView<>();

    private GameState currentState;
    private PlayerRole myRole = PlayerRole.ROLESPECTATOR;
    private PieceColor myColor = PieceColor.COLORNONE;

    public GamePane(ChessClientService clientService, Runnable onLeaveLobbyRequested) {
        this.clientService = clientService;
        this.onLeaveLobbyRequested = onLeaveLobbyRequested;

        setStyle("-fx-background-color: #1A1E24; " + FONT_FAMILY);
        setPadding(new Insets(12));

        setTop(buildTopBar());
        setCenter(buildCenterBoard());
        setRight(buildRightSidebar());

        setupEventHandlers();
    }

    public void initGame(JoinRoomResult result) {
        this.myRole = result.role;
        this.myColor = result.assignedColor;
        this.currentState = result.state;

        updateRoleBadge();
        updateGameState(result.state);
    }

    public void updateGameState(GameState state) {
        this.currentState = state;
        Platform.runLater(() -> {
            roomCodeLabel.setText("Sala: " + state.roomCode);
            spectatorsCountLabel.setText("Espectadores en sala: " + state.spectatorCount);

            // Actualizar turno y estado
            updateTurnAndStatus(state);

            // Actualizar tarjetas de jugadores
            updatePlayerCards(state);

            // Actualizar historial de movimientos
            updateMoveHistory(state);

            // Actualizar tablero de ajedrez
            boolean isSpectator = (myRole == PlayerRole.ROLESPECTATOR);
            boardControl.updateState(state, myColor, isSpectator);

            // Habilitar/deshabilitar botón rendirse
            boolean canResign = !isSpectator &&
                    (state.status == GameStatus.INPROGRESS || state.status == GameStatus.CHECKWHITE || state.status == GameStatus.CHECKBLACK);
            resignBtn.setDisable(!canResign);
        });
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #242B35; -fx-padding: 10 16 10 16; -fx-background-radius: 8;");

        roomCodeLabel.setStyle(FONT_FAMILY + " -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #64B5F6;");
        copyCodeBtn.setStyle(FONT_FAMILY + " -fx-background-color: #37474F; -fx-text-fill: white; -fx-font-size: 11px;");

        turnStatusLabel.setStyle(FONT_FAMILY + " -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        resignBtn.setStyle(FONT_FAMILY + " -fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold;");
        leaveBtn.setStyle(FONT_FAMILY + " -fx-background-color: #455A64; -fx-text-fill: white; -fx-font-weight: bold;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        bar.getChildren().addAll(
                roomCodeLabel, copyCodeBtn,
                spacer1,
                turnStatusLabel,
                spacer2,
                roleBadge,
                resignBtn,
                leaveBtn
        );

        return bar;
    }

    private StackPane buildCenterBoard() {
        StackPane pane = new StackPane(boardControl);
        pane.setPadding(new Insets(16));
        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    private VBox buildRightSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: #242B35; -fx-padding: 12; -fx-background-radius: 8;");

        // Tarjeta Jugador Superior (Negras)
        VBox topCard = createPlayerCard("Jugador Negras", topPlayerName, topPlayerTierBox, topPlayerPoints, topCapturedLabel);

        // Tarjeta Jugador Inferior (Blancas)
        VBox bottomCard = createPlayerCard("Jugador Blancas", bottomPlayerName, bottomPlayerTierBox, bottomPlayerPoints, bottomCapturedLabel);

        // Historial de jugadas
        VBox movesBox = new VBox(6);
        Label movesTitle = new Label("Historial de Jugadas");
        movesTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #90CAF9;");
        moveHistoryList.setStyle("-fx-background-color: #1A1E24; -fx-control-inner-background: #1A1E24; -fx-text-fill: white;");
        VBox.setVgrow(moveHistoryList, Priority.ALWAYS);
        movesBox.getChildren().addAll(movesTitle, moveHistoryList);
        VBox.setVgrow(movesBox, Priority.ALWAYS);

        spectatorsCountLabel.setStyle(FONT_FAMILY + " -fx-font-size: 12px; -fx-text-fill: #B0BEC5; -fx-alignment: center;");

        sidebar.getChildren().addAll(topCard, bottomCard, movesBox, spectatorsCountLabel);
        return sidebar;
    }

    private VBox createPlayerCard(String header, Label nameLbl, HBox tierBox, Label ptsLbl, Label capturedLbl) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: #1E232A; -fx-padding: 8; -fx-background-radius: 6;");

        Label head = new Label(header);
        head.setStyle(FONT_FAMILY + " -fx-font-size: 11px; -fx-text-fill: #78909C; -fx-font-weight: bold;");

        nameLbl.setStyle(FONT_FAMILY + " -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        ptsLbl.setStyle(FONT_FAMILY + " -fx-font-size: 12px; -fx-text-fill: #FFE082;");

        HBox nameRow = new HBox(8, nameLbl, tierBox);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        capturedLbl.setStyle(FONT_FAMILY + " -fx-font-size: 16px; -fx-text-fill: #E0E0E0;");

        card.getChildren().addAll(head, nameRow, ptsLbl, capturedLbl);
        return card;
    }

    private void updateRoleBadge() {
        if (myRole == PlayerRole.ROLEWHITE) {
            roleBadge.setText("T\u00DA: BLANCAS");
            roleBadge.setStyle(FONT_FAMILY + " -fx-background-color: #FFFFFF; -fx-text-fill: #1A1A1A; -fx-padding: 4 10 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
        } else if (myRole == PlayerRole.ROLEBLACK) {
            roleBadge.setText("T\u00DA: NEGRAS");
            roleBadge.setStyle(FONT_FAMILY + " -fx-background-color: #212121; -fx-text-fill: #FFFFFF; -fx-padding: 4 10 4 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-border-color: #757575; -fx-border-radius: 12;");
        } else {
            roleBadge.setText("MODO ESPECTADOR");
            roleBadge.setStyle(FONT_FAMILY + " -fx-background-color: #7B1FA2; -fx-text-fill: #FFFFFF; -fx-padding: 4 10 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
        }
    }

    private void updateTurnAndStatus(GameState state) {
        switch (state.status) {
            case WAITINGFORPLAYERS:
                turnStatusLabel.setText("Esperando que el segundo jugador se una con el c\u00F3digo...");
                turnStatusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #FFB74D; -fx-font-size: 14px; -fx-font-weight: bold;");
                break;
            case INPROGRESS:
                if (state.currentTurn == PieceColor.COLORWHITE) {
                    turnStatusLabel.setText("Turno de BLANCAS (" + state.whitePlayerNickname + ")");
                    turnStatusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #FFFFFF; -fx-font-size: 15px; -fx-font-weight: bold;");
                } else {
                    turnStatusLabel.setText("Turno de NEGRAS (" + state.blackPlayerNickname + ")");
                    turnStatusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #90CAF9; -fx-font-size: 15px; -fx-font-weight: bold;");
                }
                break;
            case CHECKWHITE:
                turnStatusLabel.setText("\u00A1JAQUE A BLANCAS! (" + state.whitePlayerNickname + ")");
                turnStatusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #FF5252; -fx-font-size: 15px; -fx-font-weight: bold;");
                break;
            case CHECKBLACK:
                turnStatusLabel.setText("\u00A1JAQUE A NEGRAS! (" + state.blackPlayerNickname + ")");
                turnStatusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #FF5252; -fx-font-size: 15px; -fx-font-weight: bold;");
                break;
            case CHECKMATEWHITEWINS:
                turnStatusLabel.setText("\u00A1JAQUE MATE! GANAN LAS BLANCAS (" + state.whitePlayerNickname + ")");
                turnStatusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #69F0AE; -fx-font-size: 16px; -fx-font-weight: bold;");
                break;
            case CHECKMATEBLACKWINS:
                turnStatusLabel.setText("\u00A1JAQUE MATE! GANAN LAS NEGRAS (" + state.blackPlayerNickname + ")");
                turnStatusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #69F0AE; -fx-font-size: 16px; -fx-font-weight: bold;");
                break;
            case STALEMATE:
                turnStatusLabel.setText("PARTIDA TERMINADA EN TABLAS (AHOGADO)");
                turnStatusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #FFD54F; -fx-font-size: 15px; -fx-font-weight: bold;");
                break;
            case RESIGNEDWHITEWINS:
                turnStatusLabel.setText("BLANCAS GANAN POR ABANDONO");
                turnStatusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #69F0AE; -fx-font-size: 15px; -fx-font-weight: bold;");
                break;
            case RESIGNEDBLACKWINS:
                turnStatusLabel.setText("NEGRAS GANAN POR ABANDONO");
                turnStatusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #69F0AE; -fx-font-size: 15px; -fx-font-weight: bold;");
                break;
            default:
                turnStatusLabel.setText("Estado: " + state.status);
                break;
        }
    }

    private void updatePlayerCards(GameState state) {
        topPlayerName.setText(state.blackPlayerNickname.isEmpty() ? "Esperando rival..." : state.blackPlayerNickname);
        topPlayerPoints.setText(state.blackPoints + " pts");
        topPlayerTierBox.getChildren().clear();
        topPlayerTierBox.getChildren().add(new TierBadge(co.icesi.chess.model.TierManager.calculateTier(state.blackPoints)));

        StringBuilder blackCaps = new StringBuilder();
        if (state.blackCapturedPieces != null) {
            for (ChessPiece p : state.blackCapturedPieces) {
                blackCaps.append(getPieceGlyph(p.type, p.color)).append(" ");
            }
        }
        topCapturedLabel.setText(blackCaps.toString());

        bottomPlayerName.setText(state.whitePlayerNickname.isEmpty() ? "-" : state.whitePlayerNickname);
        bottomPlayerPoints.setText(state.whitePoints + " pts");
        bottomPlayerTierBox.getChildren().clear();
        bottomPlayerTierBox.getChildren().add(new TierBadge(co.icesi.chess.model.TierManager.calculateTier(state.whitePoints)));

        StringBuilder whiteCaps = new StringBuilder();
        if (state.whiteCapturedPieces != null) {
            for (ChessPiece p : state.whiteCapturedPieces) {
                whiteCaps.append(getPieceGlyph(p.type, p.color)).append(" ");
            }
        }
        bottomCapturedLabel.setText(whiteCaps.toString());
    }

    private void updateMoveHistory(GameState state) {
        moveHistoryList.getItems().clear();
        if (state.moves == null) return;
        for (int i = 0; i < state.moves.length; i += 2) {
            MoveRecord mWhite = state.moves[i];
            String line = (i / 2 + 1) + ". " + mWhite.moveNotation;
            if (i + 1 < state.moves.length) {
                MoveRecord mBlack = state.moves[i + 1];
                line += "   " + mBlack.moveNotation;
            }
            moveHistoryList.getItems().add(line);
        }
        if (!moveHistoryList.getItems().isEmpty()) {
            moveHistoryList.scrollTo(moveHistoryList.getItems().size() - 1);
        }
    }

    private void setupEventHandlers() {
        copyCodeBtn.setOnAction(e -> {
            if (currentState != null && currentState.roomCode != null) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(currentState.roomCode);
                clipboard.setContent(content);
                copyCodeBtn.setText("\u00A1Copiado!");
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        Platform.runLater(() -> copyCodeBtn.setText("Copiar c\u00F3digo"));
                    } catch (InterruptedException ignored) {}
                }).start();
            }
        });

        resignBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "\u00BFSeguro que deseas rendirte?", ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirmar Rendici\u00F3n");
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        clientService.resign();
                    } catch (Exception ex) {
                        showAlert("Error", ex.getMessage());
                    }
                }
            });
        });

        leaveBtn.setOnAction(e -> {
            clientService.leaveRoom();
            if (onLeaveLobbyRequested != null) {
                onLeaveLobbyRequested.run();
            }
        });

        boardControl.setMoveListener((from, to) -> {
            try {
                boolean moved = clientService.makeMove(from, to);
                if (moved) {
                    // Actualización optimista de estado inmediato
                    GameState updated = clientService.getGameState();
                    if (updated != null) {
                        updateGameState(updated);
                    }
                }
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert("Movimiento Inv\u00E1lido", ex.getMessage()));
            }
        });
    }

    private String getPieceGlyph(PieceType type, PieceColor color) {
        if (color == PieceColor.COLORWHITE) {
            switch (type) {
                case PIECEKING:   return "\u2654";
                case PIECEQUEEN:  return "\u2655";
                case PIECEROOK:   return "\u2656";
                case PIECEBISHOP: return "\u2657";
                case PIECEKNIGHT: return "\u2658";
                case PIECEPAWN:   return "\u2659";
                default: return "";
            }
        } else {
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
