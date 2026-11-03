/**
 * Gestor de actualizaciones en tiempo real para el cliente Web mediante
 * Conexiones Bidireccionales (BiDir) sobre WebSockets en ZeroC Ice.
 * Implementa el servant ChessObserver en el navegador sin usar polling.
 */

export class ChessRealtimeObserver {
    constructor(Ice, ChessApp) {
        this.Ice = Ice;
        this.ChessApp = ChessApp;
        this.adapter = null;
        this.identity = null;
        this.onStateChangedCallback = null;
        this.onGameFinishedCallback = null;
    }

    async subscribe(communicator, servicePrx, roomCode, nickname, { onStateChanged, onGameFinished } = {}) {
        // 3.1: un solo adaptador vivo a la vez.
        await this.cleanupAdapter();
        this.onStateChangedCallback = onStateChanged || null;
        this.onGameFinishedCallback = onGameFinished || null;

        // 3.2: servant que implementa el contrato Slice ChessObserver.
        const self = this;
        class WebChessObserverServant extends this.ChessApp.ChessObserver {
            onGameStateChanged(newState, _current) {
                if (self.onStateChangedCallback) {
                    self.onStateChangedCallback(newState);
                }
            }

            onGameFinished(finalState, winnerNickname, pointsDelta, _current) {
                if (self.onGameFinishedCallback) {
                    self.onGameFinishedCallback(finalState, winnerNickname, pointsDelta);
                }
            }
        }

        // 3.3: ObjectAdapter SIN endpoints (el navegador no puede escuchar puertos).
        this.adapter = await communicator.createObjectAdapter("");
        this.identity = new this.Ice.Identity(this.Ice.generateUUID(), "ChessObserver");
        const observerBase = this.adapter.add(new WebChessObserverServant(), this.identity);
        await this.adapter.activate();

        // 3.4: BiDir -> el adaptador se asocia a la conexion WebSocket ya abierta.
        const connection = await servicePrx.ice_getConnection();
        connection.setAdapter(this.adapter);

        const observerPrx = this.ChessApp.ChessObserverPrx.uncheckedCast(observerBase);
        await servicePrx.registerObserver(roomCode, nickname, observerPrx);

        return observerPrx;
    }

    async unsubscribe(servicePrx, roomCode, nickname) {
        this.onStateChangedCallback = null;
        this.onGameFinishedCallback = null;

        if (servicePrx && roomCode && nickname) {
            try {
                await servicePrx.unregisterObserver(roomCode, nickname);
            } catch (err) {
                console.warn("No se pudo desregistrar el observador en el servidor:", err);
            }
        }

        await this.cleanupAdapter();
    }

    async cleanupAdapter() {
        if (!this.adapter) {
            this.identity = null;
            return;
        }

        try {
            if (this.identity) {
                this.adapter.remove(this.identity);
            }
            await this.adapter.destroy();
        } catch (err) {
            console.warn("Error liberando el ObjectAdapter bidireccional:", err);
        } finally {
            this.adapter = null;
            this.identity = null;
        }
    }
}