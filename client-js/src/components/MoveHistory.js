/**
 * Componente: Historial de Jugadas (MoveHistory)
 * 
 * ESPECIFICACIÓN DE REQUERIMIENTOS:
 * 
 * 1. Estructura del Componente (build()):
 *    - Contenedor principal (this.element): Debe crearse como un elemento 'div' con clase CSS 'moves-card'.
 *    - Encabezado: Debe ser un elemento 'div' con clase 'player-card-header' y texto "Historial de Jugadas".
 *    - Contenedor de lista (this.listContainer): Debe ser un elemento 'div' con id 'moves-list' y clase 'moves-list'.
 *    - Debe ensamblar el encabezado y la lista dentro de this.element, e invocar renderEmpty() al inicializarse.
 * 
 * 2. Estado Inicial / Vacío (renderEmpty()):
 *    - Debe limpiar cualquier contenido previo en this.listContainer.
 *    - Debe agregar un elemento 'em' con el texto: "Sin jugadas aún".
 * 
 * 3. Actualización de Jugadas (update(moves)):
 *    - Recibe el arreglo 'moves' (tipo MoveHistoryItem[] del contrato Slice).
 *    - Si 'moves' es nulo o su longitud es 0, debe delegar en this.renderEmpty() y finalizar.
 *    - Si existen jugadas:
 *      a. Limpiar this.listContainer.
 *      b. Iterar el arreglo agrupando las jugadas de dos en dos (turnos completos):
 *         - Número de turno: 1 para el primer par (índice 0 y 1), 2 para el segundo (índice 2 y 3), etc.
 *         - Jugada Blancas: moves[i].moveNotation (cadena de texto, ej. "e4").
 *         - Jugada Negras: moves[i + 1].moveNotation si existe (ej. "e5"), o cadena vacía si solo hubo jugada blanca.
 *      c. Por cada turno, crear una fila 'div' que contenga:
 *         - Un elemento 'strong' con el formato: "<númeroTurno>. " (ej. "1. ").
 *         - Un nodo de texto con la notación de blancas y, si existe la jugada negra, ambas separadas por espacio.
 *         - Insertar la fila dentro de this.listContainer.
 *      d. Auto-scroll: Configurar this.listContainer.scrollTop = this.listContainer.scrollHeight para asegurar
 *         que la jugada más reciente permanezca siempre a la vista del usuario.
 * 
 * 4. Obtención del Elemento DOM (getElement()):
 *    - Retornar this.element.
 */

export class MoveHistory {
    constructor() {
        this.element = document.createElement("div");
        this.listContainer = document.createElement("div");
        this.build();
    }

    build() {
        this.element.className = "moves-card";

        const header = document.createElement("div");
        header.className = "player-card-header";
        header.textContent = "Historial de Jugadas";

        this.listContainer.id = "moves-list";
        this.listContainer.className = "moves-list";

        this.element.appendChild(header);
        this.element.appendChild(this.listContainer);

        this.renderEmpty();
    }

    renderEmpty() {
        this.listContainer.replaceChildren();
        const empty = document.createElement("em");
        empty.textContent = "Sin jugadas aún";
        this.listContainer.appendChild(empty);
    }

    update(moves) {
        if (!moves || moves.length === 0) {
            this.renderEmpty();
            return;
        }

        this.listContainer.replaceChildren();

        // Agrupacion por turnos completos: blancas en el par, negras en el impar.
        for (let i = 0; i < moves.length; i += 2) {
            const turnNumber = Math.floor(i / 2) + 1;
            const whiteMove = moves[i] ? moves[i].moveNotation : "";
            const blackMove = moves[i + 1] ? moves[i + 1].moveNotation : "";

            const row = document.createElement("div");

            const label = document.createElement("strong");
            label.textContent = `${turnNumber}. `;
            row.appendChild(label);

            const notation = blackMove ? `${whiteMove} ${blackMove}` : `${whiteMove}`;
            row.appendChild(document.createTextNode(notation));

            this.listContainer.appendChild(row);
        }

        // Auto-scroll: la jugada mas reciente siempre visible.
        this.listContainer.scrollTop = this.listContainer.scrollHeight;
    }

    getElement() {
        return this.element;
    }
}
