package co.icesi.chess.client;

/**
 * Clase lanzadora (Launcher) que no extiende {@link javafx.application.Application}.
 * Evita el error "JavaFX runtime components are missing" cuando se ejecuta desde
 * el classpath o directamente desde un IDE en JDK 11+.
 */
public class ChessClientLauncher {

    public static void main(String[] args) {
        ChessClientApp.main(args);
    }
}
