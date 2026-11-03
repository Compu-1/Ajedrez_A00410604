package co.icesi.chess.client.network;

import ChessApp.*;
import com.zeroc.Ice.*;

import java.lang.Exception;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChessClientService {

    private Communicator communicator;
    private ChessServicePrx servicePrx;
    private ObjectAdapter callbackAdapter;
    private ChessObserverPrx observerPrx;

    private String currentRoomCode;
    private String currentNickname;
    private PlayerRole currentRole = PlayerRole.ROLESPECTATOR;
    private PieceColor currentAssignedColor = PieceColor.COLORNONE;

    private Consumer<GameState> stateListener;
    private BiConsumer<String, Integer> gameFinishedListener;

    public void setListeners(Consumer<GameState> stateListener, BiConsumer<String, Integer> gameFinishedListener) {
        this.stateListener = stateListener;
        this.gameFinishedListener = gameFinishedListener;
    }

    public synchronized void connect(String host, int port) throws Exception {
        disconnect();

        InitializationData initData = new InitializationData();
        initData.properties = Util.createProperties();
        initData.properties.setProperty("Ice.MessageSizeMax", "2048");

        communicator = Util.initialize(new String[0], initData);
        String proxyString = String.format("ChessService:default -h %s -p %d", host, port);
        ObjectPrx base = communicator.stringToProxy(proxyString);
        servicePrx = ChessServicePrx.checkedCast(base);

        if (servicePrx == null) {
            throw new Exception("No se pudo obtener el proxy de ChessService en " + proxyString);
        }

        // Crear adaptador de callbacks con puerto dinámico
        try {
            callbackAdapter = communicator.createObjectAdapterWithEndpoints(
                    "CallbackAdapter-" + UUID.randomUUID().toString().substring(0, 8),
                    "default"
            );
            ChessObserverImpl observerServant = new ChessObserverImpl(
                    state -> {
                        if (stateListener != null) stateListener.accept(state);
                    },
                    (winner, delta) -> {
                        if (gameFinishedListener != null) gameFinishedListener.accept(winner, delta);
                    }
            );
            ObjectPrx obsBase = callbackAdapter.addWithUUID(observerServant);
            callbackAdapter.activate();
            observerPrx = ChessObserverPrx.uncheckedCast(obsBase);
        } catch (Exception e) {
            System.err.println("[WARN] No se pudo crear adaptador de callbacks: " + e.getMessage());
        }
    }

    public synchronized String createRoom(String nickname) throws Exception {
        ensureConnected();
        this.currentNickname = nickname;
        this.currentRoomCode = servicePrx.createRoom(nickname);
        return currentRoomCode;
    }

    public synchronized JoinRoomResult joinRoom(String roomCode, String nickname) throws Exception {
        ensureConnected();
        this.currentNickname = nickname;
        this.currentRoomCode = roomCode.trim().toUpperCase();

        JoinRoomResult result = servicePrx.joinRoom(this.currentRoomCode, nickname);
        this.currentRole = result.role;
        this.currentAssignedColor = result.assignedColor;

        if (observerPrx != null) {
            try {
                servicePrx.registerObserver(this.currentRoomCode, nickname, observerPrx);
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    public synchronized boolean makeMove(Position from, Position to) throws Exception {
        ensureConnected();
        if (currentRoomCode == null) return false;
        return servicePrx.makeMove(currentRoomCode, currentNickname, from, to);
    }

    public synchronized void resign() throws Exception {
        ensureConnected();
        if (currentRoomCode == null) return;
        servicePrx.resign(currentRoomCode, currentNickname);
    }

    public synchronized void leaveRoom() {
        if (servicePrx != null && currentRoomCode != null && currentNickname != null) {
            try {
                servicePrx.leaveRoom(currentRoomCode, currentNickname);
                if (observerPrx != null) {
                    servicePrx.unregisterObserver(currentRoomCode, currentNickname);
                }
            } catch (Exception ignored) {
            }
        }
        currentRoomCode = null;
        currentRole = PlayerRole.ROLESPECTATOR;
        currentAssignedColor = PieceColor.COLORNONE;
    }

    public synchronized GameState getGameState() throws Exception {
        ensureConnected();
        if (currentRoomCode == null) return null;
        return servicePrx.getGameState(currentRoomCode);
    }

    public synchronized PlayerProfile[] getLeaderboard() throws Exception {
        ensureConnected();
        return servicePrx.getLeaderboard();
    }

    public synchronized PlayerProfile getProfile(String nickname) throws Exception {
        ensureConnected();
        return servicePrx.getProfile(nickname);
    }

    public synchronized RoomSummary[] getActiveRooms() throws Exception {
        ensureConnected();
        return servicePrx.getActiveRooms();
    }

    public synchronized boolean isConnected() {
        return communicator != null && servicePrx != null;
    }

    public synchronized void disconnect() {
        if (communicator != null) {
            try {
                leaveRoom();
                communicator.destroy();
            } catch (Exception ignored) {
            } finally {
                communicator = null;
                servicePrx = null;
                callbackAdapter = null;
                observerPrx = null;
            }
        }
    }

    private void ensureConnected() throws Exception {
        if (!isConnected()) {
            throw new Exception("El cliente no está conectado al servidor ZeroC Ice.");
        }
    }

    public String getCurrentRoomCode() {
        return currentRoomCode;
    }

    public String getCurrentNickname() {
        return currentNickname;
    }

    public PlayerRole getCurrentRole() {
        return currentRole;
    }

    public PieceColor getCurrentAssignedColor() {
        return currentAssignedColor;
    }
}
