import { getPieceGlyph } from "../services/chessRulesHelper.js";

/**
 * Componente del Tablero de Ajedrez 8x8 manipulando directamente el DOM.
 */
export class ChessBoard {
    constructor(onTileClicked) {
        this.onTileClicked = onTileClicked;
        this.element = document.createElement("div");
        this.boardGrid = document.createElement("div");
        this.tiles = [];

        this.build();
    }

    build() {
        this.element.className = "board-wrapper";
        this.boardGrid.id = "chessboard";
        this.boardGrid.className = "chessboard";

        for (let r = 0; r < 8; r++) {
            const rowTiles = [];
            for (let c = 0; c < 8; c++) {
                const tile = document.createElement("div");
                const isLight = (r + c) % 2 === 0;
                tile.className = `tile ${isLight ? "light" : "dark"}`;
                tile.dataset.row = String(r);
                tile.dataset.col = String(c);

                tile.addEventListener("click", () => {
                    if (this.onTileClicked) {
                        this.onTileClicked(r, c);
                    }
                });

                this.boardGrid.appendChild(tile);
                rowTiles.push(tile);
            }
            this.tiles.push(rowTiles);
        }

        this.element.appendChild(this.boardGrid);
    }

    /**
     * Renderiza el estado completo del tablero de ajedrez en las casillas del DOM.
     * 
     * ESPECIFICACIÓN DE REQUERIMIENTOS:
     * 
     * Parámetros:
     * - gameState: Objeto de estado remoto de ZeroC Ice (tipo GameState de Slice).
     *   * gameState.board: Matriz de 8x8 con instancias de ChessPiece (piece.type, piece.color).
     *   * gameState.moves: Arreglo de jugadas MoveHistoryItem[] (para identificar la última jugada).
     * - selectedSquare: Objeto { row, col } de la casilla actualmente seleccionada por el usuario (o null).
     * - legalMoves: Arreglo de objetos { row, col } con los destinos legales calculados (o arreglo vacío).
     * 
     * Requerimientos Funcionales:
     * 1. Validación Previa:
     *    - Si gameState o gameState.board no existen, abortar la ejecución inmediatamente sin errores.
     * 
     * 2. Identificación de la Última Jugada:
     *    - Si gameState.moves contiene elementos, extraer las coordenadas origen (lastFrom)
     *      y destino (lastTo) del último elemento del arreglo.
     * 
     * 3. Actualización de las 64 Casillas (recorrer r de 0 a 7, y c de 0 a 7):
     *    - Obtener el elemento DOM de la casilla en this.tiles[r][c] y la pieza en gameState.board[r][c].
     *    - Restablecer la clase base: "tile light" o "tile dark" según ((r + c) % 2 === 0).
     *    - Limpiar los nodos hijos previos de la casilla (tile.replaceChildren()).
     *    - Resaltar última jugada: Si (r, c) coincide con lastFrom o con lastTo, agregar la clase CSS 'last-move'.
     *    - Resaltar casilla seleccionada: Si (r, c) coincide con selectedSquare, agregar la clase CSS 'selected'.
     *    - Dibujar pieza: Si en la casilla hay una pieza distinta a PIECEEMPTY (piece.type._name !== "PIECEEMPTY"):
     *      * Crear un elemento 'span' con clase 'piece-white' si piece.color._name === "COLORWHITE",
     *        o clase 'piece-black' en caso contrario.
     *      * Asignar como textContent el glifo devuelto por getPieceGlyph(piece).
     *      * Insertar el 'span' dentro de la casilla.
     *    - Indicadores de movimiento sugerido:
     *      * Si la posición (r, c) está incluida dentro de legalMoves:
     *        - Si la casilla ya contiene una pieza, crear un elemento 'div' con clase 'capture-ring'.
     *        - Si la casilla está vacía, crear un elemento 'div' con clase 'move-dot'.
     *        - Insertar el indicador dentro de la casilla.
     */
    render(gameState, selectedSquare, legalMoves = []) {
        // RF-CB-01: sin estado remoto no hay nada que pintar.
        if (!gameState || !gameState.board) {
            return;
        }

        // RF-CB-02: coordenadas de la ultima jugada registrada en el servidor.
        let lastFrom = null;
        let lastTo = null;
        if (gameState.moves && gameState.moves.length > 0) {
            const lastMove = gameState.moves[gameState.moves.length - 1];
            lastFrom = lastMove.fromPos;
            lastTo = lastMove.toPos;
        }

        for (let r = 0; r < 8; r++) {
            for (let c = 0; c < 8; c++) {
                const tile = this.tiles[r][c];
                const piece = gameState.board[r][c];

                const isLight = (r + c) % 2 === 0;
                tile.className = `tile ${isLight ? "light" : "dark"}`;
                tile.replaceChildren();

                if ((lastFrom && lastFrom.row === r && lastFrom.col === c) ||
                    (lastTo && lastTo.row === r && lastTo.col === c)) {
                    tile.classList.add("last-move");
                }

                if (selectedSquare && selectedSquare.row === r && selectedSquare.col === c) {
                    tile.classList.add("selected");
                }

                // RF-CB-03: glifo Unicode de la pieza.
                const hasPiece = piece && piece.type && piece.type._name !== "PIECEEMPTY";
                if (hasPiece) {
                    const span = document.createElement("span");
                    const isWhite = piece.color && piece.color._name === "COLORWHITE";
                    span.className = isWhite ? "piece-white" : "piece-black";
                    span.textContent = getPieceGlyph(piece);
                    tile.appendChild(span);
                }

                // RF-CB-04: indicadores de destino candidato.
                const isLegalTarget = legalMoves.some(m => m.row === r && m.col === c);
                if (isLegalTarget) {
                    const hint = document.createElement("div");
                    hint.className = hasPiece ? "capture-ring" : "move-dot";
                    tile.appendChild(hint);
                }
            }
        }
    }

    getElement() {
        return this.element;
    }
}
