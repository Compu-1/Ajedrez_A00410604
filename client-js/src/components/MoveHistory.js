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
        // TODO: Construir la estructura DOM según la especificación del requerimiento 1.
    }

    renderEmpty() {
        // TODO: Mostrar el estado vacío según la especificación del requerimiento 2.
    }

    update(moves) {
        // TODO: Procesar el arreglo de jugadas según la especificación del requerimiento 3.
    }

    getElement() {
        return this.element;
    }
}
