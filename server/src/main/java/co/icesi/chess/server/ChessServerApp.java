package co.icesi.chess.server;
import co.icesi.chess.model.TierManager;

import co.icesi.chess.server.model.RoomManager;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.InitializationData;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class ChessServerApp {

    public static final int DEFAULT_PORT = 10000;
    public static final String SERVICE_IDENTITY = "ChessService";

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port") && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        System.out.println("===============================================================");
        System.out.println("  SERVIDOR ZEROC ICE - AJEDREZ DISTRIBUIDO (ICESI COMPU 1)     ");
        System.out.println("===============================================================");

        InitializationData initData = new InitializationData();
        initData.properties = Util.createProperties();
        // Configurar tamaño máximo de mensajes para matrices y estados
        initData.properties.setProperty("Ice.MessageSizeMax", "2048");
        initData.properties.setProperty("Ice.ThreadPool.Server.Size", "10");
        initData.properties.setProperty("Ice.ThreadPool.Server.SizeMax", "50");

        try (Communicator communicator = Util.initialize(args, initData)) {
            int wsPort = port + 2;
            String endpoint = String.format("default -p %d:ws -h 0.0.0.0 -p %d", port, wsPort);
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("ChessServerAdapter", endpoint);

            TierManager tierManager = new TierManager();
            RoomManager roomManager = new RoomManager(tierManager);
            ChessServiceImpl servant = new ChessServiceImpl(roomManager, tierManager);

            adapter.add(servant, Util.stringToIdentity(SERVICE_IDENTITY));
            adapter.activate();

            System.out.printf("[OK] Servidor escuchando en: %s%n", endpoint);
            System.out.printf("[OK] Proxy del Servicio: %s:%s%n", SERVICE_IDENTITY, endpoint);
            printNetworkInterfaces(port);
            System.out.println("[INFO] Listo para crear salas, admitir jugadores y recibir espectadores.");
            System.out.println("[INFO] Presione Ctrl+C para detener el servidor.");
            System.out.println("---------------------------------------------------------------");

            communicator.waitForShutdown();
        } catch (Exception e) {
            System.err.println("[ERROR] Error al iniciar el servidor ZeroC Ice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printNetworkInterfaces(int port) {
        try {
            System.out.println("[INFO] Direcciones IP disponibles para conexión de clientes:");
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address) {
                        System.out.printf("   -> %s (%s): %s:%s -p %d%n",
                                iface.getDisplayName(), addr.getHostAddress(), SERVICE_IDENTITY, addr.getHostAddress(), port);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
