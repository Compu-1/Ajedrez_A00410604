package co.icesi.chess.model;

import ChessApp.PlayerProfile;
import ChessApp.TierLevel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor del sistema de Tiers y Puntuación de Jugadores.
 * Clasifica a los jugadores en Bronce, Plata, Oro, Platino y Diamante.
 * Soporta persistencia local y actualización atómica concurrente.
 */
public class TierManager {

    public static final int INITIAL_POINTS = 1200;
    public static final int POINTS_WIN = 30;
    public static final int POINTS_LOSS = 20;
    public static final int POINTS_DRAW = 5;

    private final Map<String, PlayerProfile> profiles = new ConcurrentHashMap<>();
    private final Path storagePath;

    public TierManager() {
        this(Paths.get("data", "player_tiers.csv"));
    }

    public TierManager(Path storagePath) {
        this.storagePath = storagePath;
        loadFromDisk();
    }

    public static TierLevel calculateTier(int points) {
        if (points < 1200) {
            return TierLevel.TIERBRONZE;
        } else if (points < 1500) {
            return TierLevel.TIERSILVER;
        } else if (points < 1800) {
            return TierLevel.TIERGOLD;
        } else if (points < 2100) {
            return TierLevel.TIERPLATINUM;
        } else {
            return TierLevel.TIERDIAMOND;
        }
    }

    public synchronized PlayerProfile getProfile(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = "Anonimo";
        }
        String key = nickname.trim();
        return profiles.computeIfAbsent(key, k -> new PlayerProfile(
                k,
                INITIAL_POINTS,
                0,
                0,
                0,
                calculateTier(INITIAL_POINTS)
        ));
    }

    public synchronized void recordGameResult(String whitePlayer, String blackPlayer, boolean isDraw, String winnerPlayer) {
        if (whitePlayer == null || blackPlayer == null) return;

        PlayerProfile white = getProfile(whitePlayer);
        PlayerProfile black = getProfile(blackPlayer);

        if (isDraw) {
            white.points += POINTS_DRAW;
            white.draws++;
            black.points += POINTS_DRAW;
            black.draws++;
        } else if (whitePlayer.equals(winnerPlayer)) {
            white.points += POINTS_WIN;
            white.wins++;
            black.points = Math.max(0, black.points - POINTS_LOSS);
            black.losses++;
        } else if (blackPlayer.equals(winnerPlayer)) {
            black.points += POINTS_WIN;
            black.wins++;
            white.points = Math.max(0, white.points - POINTS_LOSS);
            white.losses++;
        }

        white.tier = calculateTier(white.points);
        black.tier = calculateTier(black.points);

        saveToDisk();
    }

    public synchronized PlayerProfile[] getLeaderboard() {
        List<PlayerProfile> list = new ArrayList<>(profiles.values());
        list.sort((a, b) -> {
            if (b.points != a.points) {
                return Integer.compare(b.points, a.points);
            }
            if (b.wins != a.wins) {
                return Integer.compare(b.wins, a.wins);
            }
            return a.nickname.compareToIgnoreCase(b.nickname);
        });
        return list.toArray(new PlayerProfile[0]);
    }

    private void loadFromDisk() {
        if (storagePath == null || !Files.exists(storagePath)) return;
        try (BufferedReader reader = Files.newBufferedReader(storagePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String nick = parts[0].trim();
                    int pts = Integer.parseInt(parts[1].trim());
                    int wins = Integer.parseInt(parts[2].trim());
                    int losses = Integer.parseInt(parts[3].trim());
                    int draws = Integer.parseInt(parts[4].trim());
                    TierLevel tier = calculateTier(pts);
                    profiles.put(nick, new PlayerProfile(nick, pts, wins, losses, draws, tier));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void saveToDisk() {
        if (storagePath == null) return;
        try {
            if (storagePath.getParent() != null) {
                Files.createDirectories(storagePath.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(storagePath)) {
                writer.write("# nickname,points,wins,losses,draws\n");
                for (PlayerProfile p : profiles.values()) {
                    writer.write(String.format("%s,%d,%d,%d,%d\n", p.nickname, p.points, p.wins, p.losses, p.draws));
                }
            }
        } catch (Exception ignored) {
        }
    }
}
