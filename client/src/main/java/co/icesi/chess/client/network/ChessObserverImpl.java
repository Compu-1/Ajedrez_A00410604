package co.icesi.chess.client.network;

import ChessApp.ChessObserver;
import ChessApp.GameState;
import com.zeroc.Ice.Current;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChessObserverImpl implements ChessObserver {

    private final Consumer<GameState> onStateChanged;
    private final BiConsumer<String, Integer> onFinished;

    public ChessObserverImpl(Consumer<GameState> onStateChanged, BiConsumer<String, Integer> onFinished) {
        this.onStateChanged = onStateChanged;
        this.onFinished = onFinished;
    }

    @Override
    public void onGameStateChanged(GameState newState, Current current) {
        if (onStateChanged != null) {
            onStateChanged.accept(newState);
        }
    }

    @Override
    public void onGameFinished(GameState finalState, String winnerNickname, int pointsDelta, Current current) {
        if (onFinished != null) {
            onFinished.accept(winnerNickname, pointsDelta);
        }
    }
}
