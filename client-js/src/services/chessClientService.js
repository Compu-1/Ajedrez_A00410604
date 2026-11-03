// Paso 1: el runtime de Ice publica window.Ice y el contrato generado publica window.ChessApp.
// El orden importa: ChessService.js usa window.Ice._require al evaluarse.
import "../../lib/Ice.min.js";
import "./ChessService.js";
import { ChessRealtimeObserver } from "./chessRealtimeObserver.js";

export class ChessClientService {
    constructor() {
        this.Ice = window.Ice;
        this.ChessApp = window.ChessApp;

        this.communicator = null;
        this.service = null;
        this.realtimeObserver = null;

        this.isConnected = false;
        this.nickname = null;
        this.currentRoomCode = null;
        this.role = null;
        this.assignedColor = null;
    }

    validateRuntime() {
        this.Ice = this.Ice || window.Ice;
        this.ChessApp = this.ChessApp || window.ChessApp;

        if (!this.Ice) {
            throw new Error("El runtime de ZeroC Ice no está disponible. Verifica la carga de lib/Ice.min.js.");
        }
        if (!this.ChessApp || !this.ChessApp.ChessServicePrx) {
            throw new Error(
                "El contrato ChessApp no está disponible. Genera src/services/ChessService.js con slice2js."
            );
        }
    }

    async connect(host, port, nickname) {
        this.validateRuntime();

        if (this.communicator) {
            try {
                await this.communicator.destroy();
            } catch (err) {
                console.warn("Error destruyendo el communicator previo:", err);
            }
            this.communicator = null;
            this.service = null;
            this.isConnected = false;
        }

        const targetHost = (host && String(host).trim()) || "127.0.0.1";
        const targetPort = (port && String(port).trim()) || "10002";

        const initData = new this.Ice.InitializationData();
        initData.properties = this.Ice.createProperties();
        initData.properties.setProperty("Ice.MessageSizeMax", "2048");
        // ACM sin timeout + heartbeat siempre: mantiene viva la conexion BiDir.
        initData.properties.setProperty("Ice.ACM.Client.Timeout", "0");
        initData.properties.setProperty("Ice.ACM.Client.Heartbeat", "3");

        this.communicator = this.Ice.initialize(initData);

        const proxyString = `ChessService:ws -h ${targetHost} -p ${targetPort}`;
        const base = this.communicator.stringToProxy(proxyString);
        this.service = await this.ChessApp.ChessServicePrx.checkedCast(base);

        if (!this.service) {
            throw new Error(`El proxy remoto no respondió en ${proxyString}`);
        }

        this.nickname = nickname;
        this.realtimeObserver = new ChessRealtimeObserver(this.Ice, this.ChessApp);
        this.isConnected = true;

        return this.service;
    }

    async getLobbyData() {
        this.ensureConnected();
        const [rooms, leaderboard] = await Promise.all([
            this.service.getActiveRooms(),
            this.service.getLeaderboard()
        ]);
        return { rooms: rooms || [], leaderboard: leaderboard || [] };
    }

    async createRoom(nickname) {
        this.ensureConnected();
        if (nickname) {
            this.nickname = nickname;
        }
        return await this.service.createRoom(this.nickname);
    }

    async joinRoom(roomCode, nickname) {
        this.ensureConnected();
        if (nickname) {
            this.nickname = nickname;
        }

        const normalizedCode = String(roomCode).trim().toUpperCase();
        const result = await this.service.joinRoom(normalizedCode, this.nickname);

        this.currentRoomCode = normalizedCode;
        this.role = result.role;
        this.assignedColor = result.assignedColor;

        return result;
    }

    async subscribeToRoom(listeners = {}) {
        this.ensureConnected();
        if (!this.currentRoomCode) {
            throw new Error("No hay una sala activa para suscribirse.");
        }
        return await this.realtimeObserver.subscribe(
            this.communicator,
            this.service,
            this.currentRoomCode,
            this.nickname,
            listeners
        );
    }

    async makeMove(fromRow, fromCol, toRow, toCol) {
        this.ensureConnected();
        if (!this.currentRoomCode) {
            return false;
        }
        const fromPos = new this.ChessApp.Position(fromRow, fromCol);
        const toPos = new this.ChessApp.Position(toRow, toCol);
        return await this.service.makeMove(this.currentRoomCode, this.nickname, fromPos, toPos);
    }

    async resign() {
        this.ensureConnected();
        if (!this.currentRoomCode) {
            return;
        }
        await this.service.resign(this.currentRoomCode, this.nickname);
    }

    async leaveRoom() {
        if (!this.isConnected || !this.currentRoomCode) {
            return;
        }

        const roomCode = this.currentRoomCode;
        try {
            if (this.realtimeObserver) {
                await this.realtimeObserver.unsubscribe(this.service, roomCode, this.nickname);
            }
            await this.service.leaveRoom(roomCode, this.nickname);
        } catch (err) {
            console.warn("Error saliendo de la sala:", err);
        } finally {
            this.currentRoomCode = null;
            this.role = null;
            this.assignedColor = null;
        }
    }

    ensureConnected() {
        if (!this.isConnected || !this.service) {
            throw new Error("El cliente no está conectado al servidor ZeroC Ice.");
        }
    }
}