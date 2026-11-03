package co.icesi.chess.server.model;
import co.icesi.chess.model.TierManager;

import ChessApp.RoomNotFoundException;
import ChessApp.RoomSummary;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Administra las salas de juego concurrentes en el servidor ZeroC Ice.
 */
public class RoomManager {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 5;
    private final SecureRandom random = new SecureRandom();

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final TierManager tierManager;

    public RoomManager(TierManager tierManager) {
        this.tierManager = tierManager;
    }

    public synchronized String createRoom(String creatorNickname) {
        String code;
        do {
            code = generateCode();
        } while (rooms.containsKey(code));

        GameRoom room = new GameRoom(code, creatorNickname, tierManager);
        rooms.put(code, room);
        return code;
    }

    public GameRoom getRoom(String roomCode) throws RoomNotFoundException {
        if (roomCode == null) {
            throw new RoomNotFoundException("Código de sala no proporcionado.");
        }
        String normalized = roomCode.trim().toUpperCase();
        GameRoom room = rooms.get(normalized);
        if (room == null) {
            throw new RoomNotFoundException("No se encontró la sala con código: " + normalized);
        }
        return room;
    }

    public RoomSummary[] getActiveRooms() {
        List<RoomSummary> list = new ArrayList<>();
        for (GameRoom r : rooms.values()) {
            list.add(r.getSummary());
        }
        return list.toArray(new RoomSummary[0]);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder("R-");
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }
}
