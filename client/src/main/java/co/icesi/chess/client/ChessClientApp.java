package co.icesi.chess.client;

import ChessApp.GameState;
import ChessApp.JoinRoomResult;
import co.icesi.chess.client.network.ChessClientService;
import co.icesi.chess.client.ui.GamePane;
import co.icesi.chess.client.ui.LobbyPane;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ChessClientApp extends Application {

    private final ChessClientService clientService = new ChessClientService();
    private Stage primaryStage;
    private LobbyPane lobbyPane;
    private GamePane gamePane;
    private Scene scene;

    private Timeline pollingTimeline;
    private long lastObservedVersion = -1;
    private boolean inGame = false;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Icesi Ajedrez Distribuido - ZeroC Ice & JavaFX");

        lobbyPane = new LobbyPane(clientService);
        gamePane = new GamePane(clientService, this::returnToLobby);

        lobbyPane.setOnRoomJoined(this::enterGame);

        // Configurar escuchadores asíncronos de eventos Ice
        clientService.setListeners(
                this::handleRemoteStateUpdate,
                this::handleGameFinished
        );

        // Configurar sincronizador periódico de respaldo (Failsafe polling)
        setupBackgroundSync();

        scene = new Scene(lobbyPane, 960, 680);
        stage.setScene(scene);
        stage.setMinWidth(880);
        stage.setMinHeight(640);

        stage.setOnCloseRequest(e -> {
            stopBackgroundSync();
            clientService.disconnect();
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }

    private void enterGame(JoinRoomResult result) {
        inGame = true;
        lastObservedVersion = result.state.version;
        gamePane.initGame(result);
        scene.setRoot(gamePane);
    }

    private void returnToLobby() {
        inGame = false;
        lastObservedVersion = -1;
        lobbyPane.refreshData();
        scene.setRoot(lobbyPane);
    }

    private void handleRemoteStateUpdate(GameState newState) {
        if (inGame && newState != null) {
            lastObservedVersion = newState.version;
            gamePane.updateGameState(newState);
        }
    }

    private void handleGameFinished(String winner, int pointsDelta) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Partida Finalizada");
            alert.setHeaderText("¡Fin de la Partida!");
            String msg = (winner == null || winner.isEmpty())
                    ? "La partida ha concluido en tablas (+5 pts para ambos)."
                    : String.format("¡Ganador: %s! (+%d pts)", winner, pointsDelta);
            alert.setContentText(msg);
            alert.show();
        });
    }

    private void setupBackgroundSync() {
        pollingTimeline = new Timeline(new KeyFrame(Duration.millis(750), e -> {
            if (inGame && clientService.isConnected()) {
                new Thread(() -> {
                    try {
                        GameState state = clientService.getGameState();
                        if (state != null && state.version > lastObservedVersion) {
                            lastObservedVersion = state.version;
                            gamePane.updateGameState(state);
                        }
                    } catch (Exception ignored) {
                    }
                }).start();
            }
        }));
        pollingTimeline.setCycleCount(Animation.INDEFINITE);
        pollingTimeline.play();
    }

    private void stopBackgroundSync() {
        if (pollingTimeline != null) {
            pollingTimeline.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
