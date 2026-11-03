package co.icesi.chess.client.ui;

import ChessApp.*;
import co.icesi.chess.client.network.ChessClientService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Consumer;

public class LobbyPane extends BorderPane {

    private static final String FONT_FAMILY = "-fx-font-family: 'Segoe UI', 'DejaVu Sans', Arial, sans-serif;";

    private final ChessClientService clientService;
    private Consumer<JoinRoomResult> onRoomJoinedListener;

    private final TextField hostField = new TextField("localhost");
    private final TextField portField = new TextField("10000");
    private final TextField nickField = new TextField("Jugador_" + (int)(Math.random() * 900 + 100));
    private final Button connectBtn = new Button("Conectar");
    private final Label statusLabel = new Label("Desconectado");

    private final Button createRoomBtn = new Button("Crear Sala");
    private final TextField roomCodeField = new TextField();
    private final Button joinRoomBtn = new Button("Unirse a Sala");

    private final TableView<RoomSummary> roomsTable = new TableView<>();
    private final TableView<PlayerProfile> leaderboardTable = new TableView<>();

    public LobbyPane(ChessClientService clientService) {
        this.clientService = clientService;
        setStyle("-fx-background-color: #1E232A; " + FONT_FAMILY);
        setPadding(new Insets(16));

        setTop(buildHeader());
        setCenter(buildCenterContent());

        setupEventHandlers();
        setControlsEnabled(false);
    }

    public void setOnRoomJoined(Consumer<JoinRoomResult> listener) {
        this.onRoomJoinedListener = listener;
    }

    private VBox buildHeader() {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(0, 0, 16, 0));

        Label title = new Label("AJEDREZ DISTRIBUIDO ZEROC ICE");
        title.setStyle(FONT_FAMILY + " -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Label subtitle = new Label("Computaci\u00F3n en Internet I (09776 - TIC) - Salas, Espectadores y Clasificaci\u00F3n por Tiers");
        subtitle.setStyle(FONT_FAMILY + " -fx-font-size: 13px; -fx-text-fill: #9E9E9E;");

        box.getChildren().addAll(title, subtitle);
        return box;
    }

    private VBox buildCenterContent() {
        VBox box = new VBox(14);
        box.setAlignment(Pos.TOP_CENTER);

        // Barra de conexión
        HBox connBar = new HBox(10);
        connBar.setAlignment(Pos.CENTER);
        connBar.setStyle("-fx-background-color: #262D37; -fx-padding: 12; -fx-background-radius: 8;");

        hostField.setPrefWidth(120);
        portField.setPrefWidth(70);
        nickField.setPrefWidth(140);
        connectBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        statusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");

        connBar.getChildren().addAll(
                new Label("Servidor:"), hostField,
                new Label("Puerto:"), portField,
                new Label("Nick:"), nickField,
                connectBtn,
                statusLabel
        );
        for (javafx.scene.Node node : connBar.getChildren()) {
            if (node instanceof Label && node != statusLabel) {
                ((Label) node).setStyle("-fx-text-fill: #E0E0E0; -fx-font-weight: bold;");
            }
        }

        // Barra de acciones de sala
        HBox roomBar = new HBox(12);
        roomBar.setAlignment(Pos.CENTER);
        roomBar.setStyle("-fx-background-color: #262D37; -fx-padding: 12; -fx-background-radius: 8;");

        createRoomBtn.setStyle(FONT_FAMILY + " -fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        roomCodeField.setPromptText("C\u00F3digo (ej. R-ABCD)");
        roomCodeField.setPrefWidth(140);
        joinRoomBtn.setStyle(FONT_FAMILY + " -fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold;");

        Button refreshBtn = new Button("Actualizar Datos");
        refreshBtn.setStyle(FONT_FAMILY + " -fx-background-color: #455A64; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> refreshData());

        Label orLabel = new Label("  o  ");
        orLabel.setStyle("-fx-text-fill: #E0E0E0;");

        roomBar.getChildren().addAll(
                createRoomBtn,
                orLabel,
                roomCodeField,
                joinRoomBtn,
                new Region(),
                refreshBtn
        );
        HBox.setHgrow(roomBar.getChildren().get(4), Priority.ALWAYS);

        // Tablas divididas (Salas activas y Tabla de Tiers)
        SplitPane splitPane = new SplitPane();
        splitPane.setStyle("-fx-box-border: transparent; -fx-background-color: transparent;");

        VBox roomsBox = new VBox(8);
        Label roomsLabel = new Label("Salas de Juego Activas");
        roomsLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #90CAF9;");
        setupRoomsTable();
        roomsBox.getChildren().addAll(roomsLabel, roomsTable);
        VBox.setVgrow(roomsTable, Priority.ALWAYS);

        VBox leaderBox = new VBox(8);
        Label leaderLabel = new Label("Tabla Global de Clasificaci\u00F3n y Tiers");
        leaderLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #FFE082;");
        setupLeaderboardTable();
        leaderBox.getChildren().addAll(leaderLabel, leaderboardTable);
        VBox.setVgrow(leaderboardTable, Priority.ALWAYS);

        splitPane.getItems().addAll(roomsBox, leaderBox);
        splitPane.setDividerPositions(0.48);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        box.getChildren().addAll(connBar, roomBar, splitPane);
        return box;
    }

    private void setupRoomsTable() {
        roomsTable.setPlaceholder(new Label("No hay salas activas creadas. \u00A1Crea una para comenzar!"));

        TableColumn<RoomSummary, String> codeCol = new TableColumn<>("C\u00F3digo");
        codeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().roomCode));
        codeCol.setPrefWidth(90);

        TableColumn<RoomSummary, String> whiteCol = new TableColumn<>("Blancas");
        whiteCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().whitePlayer));
        whiteCol.setPrefWidth(110);

        TableColumn<RoomSummary, String> blackCol = new TableColumn<>("Negras");
        blackCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().blackPlayer));
        blackCol.setPrefWidth(110);

        TableColumn<RoomSummary, String> specsCol = new TableColumn<>("Espectadores");
        specsCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().spectatorCount)));
        specsCol.setPrefWidth(90);

        TableColumn<RoomSummary, String> statusCol = new TableColumn<>("Estado");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(formatStatus(d.getValue().status)));
        statusCol.setPrefWidth(120);

        TableColumn<RoomSummary, Void> actionCol = new TableColumn<>("Acci\u00F3n");
        actionCol.setPrefWidth(80);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Entrar");
            {
                btn.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-size: 11px;");
                btn.setOnAction(e -> {
                    RoomSummary item = getTableView().getItems().get(getIndex());
                    joinExistingRoom(item.roomCode);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        roomsTable.getColumns().addAll(codeCol, whiteCol, blackCol, specsCol, statusCol, actionCol);
    }

    private void setupLeaderboardTable() {
        leaderboardTable.setPlaceholder(new Label("No hay registros de jugadores."));

        TableColumn<PlayerProfile, String> nickCol = new TableColumn<>("Jugador");
        nickCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nickname));
        nickCol.setPrefWidth(110);

        TableColumn<PlayerProfile, String> ptsCol = new TableColumn<>("Puntos");
        ptsCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().points)));
        ptsCol.setPrefWidth(70);

        TableColumn<PlayerProfile, Void> tierCol = new TableColumn<>("Tier / Rango");
        tierCol.setPrefWidth(120);
        tierCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    PlayerProfile profile = getTableView().getItems().get(getIndex());
                    setGraphic(new TierBadge(profile.tier));
                }
            }
        });

        TableColumn<PlayerProfile, String> recordCol = new TableColumn<>("V / D / E");
        recordCol.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%d / %d / %d", d.getValue().wins, d.getValue().losses, d.getValue().draws)
        ));
        recordCol.setPrefWidth(90);

        leaderboardTable.getColumns().addAll(nickCol, ptsCol, tierCol, recordCol);
    }

    private void setupEventHandlers() {
        connectBtn.setOnAction(e -> {
            String host = hostField.getText().trim();
            int port = 10000;
            try {
                port = Integer.parseInt(portField.getText().trim());
            } catch (NumberFormatException ignored) {}

            try {
                statusLabel.setText("Conectando...");
                clientService.connect(host, port);
                statusLabel.setText("Conectado");
                statusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                connectBtn.setDisable(true);
                hostField.setDisable(true);
                portField.setDisable(true);
                setControlsEnabled(true);
                refreshData();
            } catch (Exception ex) {
                statusLabel.setText("Error de conexi\u00F3n");
                statusLabel.setStyle(FONT_FAMILY + " -fx-text-fill: #F44336; -fx-font-weight: bold;");
                showAlert("Error al Conectar", ex.getMessage());
            }
        });

        createRoomBtn.setOnAction(e -> {
            String nick = nickField.getText().trim();
            if (nick.isEmpty()) nick = "Jugador";
            try {
                String code = clientService.createRoom(nick);
                JoinRoomResult result = clientService.joinRoom(code, nick);
                if (onRoomJoinedListener != null) {
                    onRoomJoinedListener.accept(result);
                }
            } catch (Exception ex) {
                showAlert("Error al Crear Sala", ex.getMessage());
            }
        });

        joinRoomBtn.setOnAction(e -> {
            String code = roomCodeField.getText().trim();
            if (code.isEmpty()) {
                showAlert("C\u00F3digo Requerido", "Ingresa el c\u00F3digo de la sala.");
                return;
            }
            joinExistingRoom(code);
        });
    }

    private void joinExistingRoom(String code) {
        String nick = nickField.getText().trim();
        if (nick.isEmpty()) nick = "Jugador";
        try {
            JoinRoomResult result = clientService.joinRoom(code, nick);
            if (onRoomJoinedListener != null) {
                onRoomJoinedListener.accept(result);
            }
        } catch (Exception ex) {
            showAlert("Error al Unirse a la Sala", ex.getMessage());
        }
    }

    public void refreshData() {
        if (!clientService.isConnected()) return;
        new Thread(() -> {
            try {
                RoomSummary[] rooms = clientService.getActiveRooms();
                PlayerProfile[] leaderboard = clientService.getLeaderboard();
                Platform.runLater(() -> {
                    roomsTable.getItems().setAll(rooms);
                    leaderboardTable.getItems().setAll(leaderboard);
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void setControlsEnabled(boolean enabled) {
        createRoomBtn.setDisable(!enabled);
        roomCodeField.setDisable(!enabled);
        joinRoomBtn.setDisable(!enabled);
        roomsTable.setDisable(!enabled);
        leaderboardTable.setDisable(!enabled);
    }

    private String formatStatus(GameStatus status) {
        if (status == null) return "-";
        switch (status) {
            case WAITINGFORPLAYERS: return "Esperando Rival";
            case INPROGRESS: return "En Curso";
            case CHECKWHITE: return "Jaque Blancas";
            case CHECKBLACK: return "Jaque Negras";
            case CHECKMATEWHITEWINS: return "Ganan Blancas (Mate)";
            case CHECKMATEBLACKWINS: return "Ganan Negras (Mate)";
            case STALEMATE: return "Tablas (Ahogado)";
            case RESIGNEDWHITEWINS: return "Ganan Blancas (Abandono)";
            case RESIGNEDBLACKWINS: return "Ganan Negras (Abandono)";
            case ABORTED: return "Cancelada";
            default: return status.toString();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
