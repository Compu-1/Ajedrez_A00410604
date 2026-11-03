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

    /**
     * Crea el servant de ChessObserver, configura el ObjectAdapter bidireccional
     * sobre la conexión WebSocket activa y registra el observador en la sala.
     */
    async subscribe(communicator, servicePrx, roomCode, nickname, { onStateChanged, onGameFinished } = {}) {
        // TODO (Paso 3.1): Limpiar cualquier adaptador previo invocando await this.cleanupAdapter()
        // y almacenar los callbacks onStateChanged y onGameFinished.

        // TODO (Paso 3.2): Definir una clase servant (ej. WebChessObserverServant) que extienda
        // this.ChessApp.ChessObserver e implemente:
        //   - onGameStateChanged(newState, _current)
        //   - onGameFinished(finalState, winnerNickname, pointsDelta, _current)

        // TODO (Paso 3.3): Crear un ObjectAdapter sin endpoints con communicator.createObjectAdapter(""),
        // generar una identidad única con new this.Ice.Identity(this.Ice.generateUUID(), "ChessObserver"),
        // registrar la instancia del servant con this.adapter.add(...) y activar el adaptador.

        // TODO (Paso 3.4): Obtener la conexión WebSocket activa con await servicePrx.ice_getConnection(),
        // asociarle el adaptador con connection.setAdapter(this.adapter), crear el proxy del observador
        // con this.ChessApp.ChessObserverPrx.uncheckedCast(...) e invocar:
        // await servicePrx.registerObserver(roomCode, nickname, observerPrx);
    }

    /**
     * Desvincula el observador de la sala en el servidor y limpia el adaptador local.
     */
    async unsubscribe(servicePrx, roomCode, nickname) {
        // TODO (Paso 3.5): Limpiar los callbacks locales, invocar
        // await servicePrx.unregisterObserver(roomCode, nickname) (manejando posibles excepciones)
        // y llamar a await this.cleanupAdapter().
    }

    async cleanupAdapter() {
        // TODO (Paso 3.6): Si this.adapter existe, remover this.identity y destruir el adaptador
        // (await this.adapter.destroy()), dejando this.adapter = null y this.identity = null.
    }
}
