package co.icesi.chess.server;

import ChessApp.*;
import co.icesi.chess.model.TierManager;
import co.icesi.chess.server.model.RoomManager;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class ChessIntegrationTest {

    private RoomManager roomManager;
    private TierManager tierManager;
    private ChessServiceImpl service;

    @Before
    public void setUp() throws Exception {
        Path tempFile = Files.createTempFile("test_tiers", ".csv");
        tempFile.toFile().deleteOnExit();
        tierManager = new TierManager(tempFile);
        roomManager = new RoomManager(tierManager);
        service = new ChessServiceImpl(roomManager, tierManager);
    }

    @Test
    public void testFullRoomFlowPlayersAndSpectators() throws Exception {
        // 1. Creador crea sala
        String roomCode = service.createRoom("Alice", null);
        assertNotNull(roomCode);
        assertTrue(roomCode.startsWith("R-"));

        // 2. Alice se une -> Blancas
        JoinRoomResult joinAlice = service.joinRoom(roomCode, "Alice", null);
        assertEquals(PlayerRole.ROLEWHITE, joinAlice.role);
        assertEquals(PieceColor.COLORWHITE, joinAlice.assignedColor);
        assertEquals(GameStatus.WAITINGFORPLAYERS, joinAlice.state.status);

        // 3. Bob se une -> Negras
        JoinRoomResult joinBob = service.joinRoom(roomCode, "Bob", null);
        assertEquals(PlayerRole.ROLEBLACK, joinBob.role);
        assertEquals(PieceColor.COLORBLACK, joinBob.assignedColor);
        assertEquals(GameStatus.INPROGRESS, joinBob.state.status);

        // 4. Charlie se une -> Ya hay 2 jugadores, debe ser ESPECTADOR
        JoinRoomResult joinCharlie = service.joinRoom(roomCode, "Charlie", null);
        assertEquals(PlayerRole.ROLESPECTATOR, joinCharlie.role);
        assertEquals(PieceColor.COLORNONE, joinCharlie.assignedColor);
        assertEquals(GameStatus.INPROGRESS, joinCharlie.state.status);
        assertEquals(1, joinCharlie.state.spectatorCount);

        // 5. David se une -> Segundo ESPECTADOR
        JoinRoomResult joinDavid = service.joinRoom(roomCode, "David", null);
        assertEquals(PlayerRole.ROLESPECTATOR, joinDavid.role);
        assertEquals(2, joinDavid.state.spectatorCount);

        // 6. Validar turnos: Intento de mover por Bob (Negras) o Charlie (Espectador) debe fallar
        try {
            service.makeMove(roomCode, "Bob", new Position(1, 4), new Position(3, 4), null);
            fail("Bob no debe poder mover cuando es el turno de Blancas");
        } catch (NotPlayerTurnException expected) {
            // Esperado
        }

        try {
            service.makeMove(roomCode, "Charlie", new Position(6, 4), new Position(4, 4), null);
            fail("Un espectador nunca debe poder mover piezas");
        } catch (NotPlayerTurnException expected) {
            // Esperado
        }

        // 7. Alice mueve peón e2 (6,4) a e4 (4,4)
        boolean moved = service.makeMove(roomCode, "Alice", new Position(6, 4), new Position(4, 4), null);
        assertTrue(moved);

        GameState stateAfterAlice = service.getGameState(roomCode, null);
        assertEquals(PieceColor.COLORBLACK, stateAfterAlice.currentTurn);
        assertEquals(PieceType.PIECEPAWN, stateAfterAlice.board[4][4].type);
        assertEquals(PieceColor.COLORWHITE, stateAfterAlice.board[4][4].color);
        assertTrue(stateAfterAlice.board[6][4].type == PieceType.PIECEEMPTY);
        assertEquals(1, stateAfterAlice.moves.length);
        assertEquals("e4", stateAfterAlice.moves[0].moveNotation);

        // 8. Bob mueve peón e7 (1,4) a e5 (3,4)
        boolean movedBob = service.makeMove(roomCode, "Bob", new Position(1, 4), new Position(3, 4), null);
        assertTrue(movedBob);

        GameState stateAfterBob = service.getGameState(roomCode, null);
        assertEquals(PieceColor.COLORWHITE, stateAfterBob.currentTurn);
        assertEquals(2, stateAfterBob.moves.length);

        // 9. Bob se rinde -> Gana Alice
        service.resign(roomCode, "Bob", null);
        GameState finalState = service.getGameState(roomCode, null);
        assertEquals(GameStatus.RESIGNEDWHITEWINS, finalState.status);

        // 10. Validar actualización de Tiers y Puntos
        PlayerProfile profileAlice = service.getProfile("Alice", null);
        PlayerProfile profileBob = service.getProfile("Bob", null);

        // Alice gana 30 puntos (1200 + 30 = 1230)
        assertEquals(1230, profileAlice.points);
        assertEquals(1, profileAlice.wins);
        assertEquals(TierLevel.TIERSILVER, profileAlice.tier);

        // Bob pierde 20 puntos (1200 - 20 = 1180)
        assertEquals(1180, profileBob.points);
        assertEquals(1, profileBob.losses);
        assertEquals(TierLevel.TIERBRONZE, profileBob.tier);

        // 11. Validar Leaderboard
        PlayerProfile[] leaderboard = service.getLeaderboard(null);
        assertTrue(leaderboard.length >= 2);
        assertEquals("Alice", leaderboard[0].nickname);
        assertEquals(1230, leaderboard[0].points);
        assertEquals("Bob", leaderboard[1].nickname);
        assertEquals(1180, leaderboard[1].points);
    }
}
