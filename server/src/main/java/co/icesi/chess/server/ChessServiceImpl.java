package co.icesi.chess.server;

import ChessApp.*;
import co.icesi.chess.server.model.GameRoom;
import co.icesi.chess.server.model.RoomManager;
import co.icesi.chess.model.TierManager;
import com.zeroc.Ice.Current;

public class ChessServiceImpl implements ChessService {

    private final RoomManager roomManager;
    private final TierManager tierManager;

    public ChessServiceImpl(RoomManager roomManager, TierManager tierManager) {
        this.roomManager = roomManager;
        this.tierManager = tierManager;
    }

    @Override
    public PlayerProfile getProfile(String nickname, Current current) {
        return tierManager.getProfile(nickname);
    }

    @Override
    public PlayerProfile[] getLeaderboard(Current current) {
        return tierManager.getLeaderboard();
    }

    @Override
    public String createRoom(String creatorNickname, Current current) {
        return roomManager.createRoom(creatorNickname);
    }

    @Override
    public JoinRoomResult joinRoom(String roomCode, String nickname, Current current)
            throws ChessException, RoomNotFoundException {
        GameRoom room = roomManager.getRoom(roomCode);
        return room.join(nickname);
    }

    @Override
    public void registerObserver(String roomCode, String nickname, ChessObserverPrx observer, Current current) {
        try {
            GameRoom room = roomManager.getRoom(roomCode);
            // TODO (Paso 2 del Taller): Soportar conexiones bidireccionales (BiDir) desde WebSockets.
            // Cuando el cliente Web registra un proxy sin endpoints (observer.ice_getEndpoints().length == 0),
            // vincular el proxy a la conexion entrante actual (current.con) usando observer.ice_fixed(current.con).
            room.registerObserver(nickname, observer);
        } catch (RoomNotFoundException ignored) {
        }
    }

    @Override
    public void unregisterObserver(String roomCode, String nickname, Current current) {
        try {
            GameRoom room = roomManager.getRoom(roomCode);
            room.unregisterObserver(nickname);
        } catch (RoomNotFoundException ignored) {
        }
    }

    @Override
    public GameState getGameState(String roomCode, Current current) throws RoomNotFoundException {
        GameRoom room = roomManager.getRoom(roomCode);
        return room.getGameStateSnapshot();
    }

    @Override
    public boolean makeMove(String roomCode, String nickname, Position fromPos, Position toPos, Current current)
            throws InvalidMoveException, NotPlayerTurnException, RoomNotFoundException {
        GameRoom room = roomManager.getRoom(roomCode);
        return room.makeMove(nickname, fromPos, toPos);
    }

    @Override
    public void resign(String roomCode, String nickname, Current current)
            throws RoomNotFoundException, UnauthorizedActionException {
        GameRoom room = roomManager.getRoom(roomCode);
        room.resign(nickname);
    }

    @Override
    public void leaveRoom(String roomCode, String nickname, Current current) throws RoomNotFoundException {
        GameRoom room = roomManager.getRoom(roomCode);
        room.leave(nickname);
    }

    @Override
    public RoomSummary[] getActiveRooms(Current current) {
        return roomManager.getActiveRooms();
    }
}
