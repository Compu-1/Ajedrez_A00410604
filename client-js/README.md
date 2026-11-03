# Cliente Web en JavaScript Vainilla - Ajedrez Distribuido ZeroC Ice

Este módulo implementa el frontend web completo en **JavaScript Puro (Vanilla JS)** componetizado para el sistema de Ajedrez Distribuido con ZeroC Ice, sin frameworks externos (React, Angular o Vue).

---

## Arquitectura Componetizada (`src/`)

El archivo `index.html` contiene únicamente el contenedor raíz `<div id="root"></div>`, y toda la interfaz se construye y manipula dinámicamente mediante la API del DOM (`document.createElement`, eventos y actualización reactiva de estado) organizada en tres capas:

- `src/app.js`: Punto de entrada y orquestador principal (`ChessApplication`) que monta las vistas sobre `#root`.
- `src/components/`:
  - `Header.js`: Barra superior y estado de conexión.
  - `LobbyView.js`: Vista de conexión, creación/unión a salas por código, tabla de salas activas y tabla global de clasificación.
  - `GameView.js`: Vista principal de partida en curso.
  - `ChessBoard.js`: Tablero interactivo 8x8 con resaltado de jugadas y sugerencia de movimientos legales.
  - `PlayerCard.js`: Tarjeta reutilizable de jugador con puntos, piezas capturadas e insignia de Tier.
  - `MoveHistory.js`: Lista de movimientos en notación algebraica.
  - `TierBadge.js`: Insignia visual del rango del jugador (Bronce, Plata, Oro, Platino, Diamante).
- `src/services/`:
  - `chessClientService.js`: Gestión del `Communicator` de ZeroC Ice por WebSockets (`ws://127.0.0.1:10002`), invocación de métodos remotos sobre `ChessServicePrx` y sincronización en vivo.
  - `chessRulesHelper.js`: Cálculo local de movimientos candidatos y utilidades de formato.

---

## Instrucciones de Ejecución

### 1. Iniciar el Servidor ZeroC Ice (desde `codigo_base/`):
```bash
./gradlew runServer
```
*El servidor escuchará en el puerto TCP `10000` (JavaFX) y WebSocket `10002` (JavaScript).*

### 2. Iniciar el Frontend Web en JavaScript:
```bash
cd client-js
npm install
npm start
```

Abre en tu navegador:
`http://localhost:3000`

---

## Recompilar el Contrato Slice (Opcional):
Si modificas el archivo `common/src/main/slice/ChessService.ice`, puedes regenerar `ChessService.js` ejecutando:
```bash
npm run generate-slice
```
