import { TierBadge } from "./TierBadge.js";
import { calculateTierFromPoints, getPieceGlyph } from "../services/chessRulesHelper.js";

/**
 * Componente de Tarjeta de Jugador (Blancas / Negras) en la vista de partida.
 */
export class PlayerCard {
    constructor(headerTitle, defaultName, capturedColorKey) {
        this.headerTitle = headerTitle;
        this.defaultName = defaultName;
        this.capturedColorKey = capturedColorKey;

        this.element = document.createElement("div");
        this.nameEl = document.createElement("span");
        this.tierBadge = new TierBadge({ _name: "TIERSILVER" });
        this.pointsEl = document.createElement("div");
        this.capturedEl = document.createElement("div");

        this.build();
    }

    build() {
        this.element.className = "player-card";

        const header = document.createElement("div");
        header.className = "player-card-header";
        header.textContent = this.headerTitle;

        const body = document.createElement("div");
        body.className = "player-card-body";

        this.nameEl.className = "player-name";
        this.nameEl.textContent = this.defaultName;

        body.appendChild(this.nameEl);
        body.appendChild(this.tierBadge.getElement());

        this.pointsEl.className = "player-pts";
        this.pointsEl.textContent = "1200 pts";

        this.capturedEl.className = "captured-pieces";

        this.element.appendChild(header);
        this.element.appendChild(body);
        this.element.appendChild(this.pointsEl);
        this.element.appendChild(this.capturedEl);
    }

    /**
     * Actualiza la información reactiva del jugador a partir del estado de la partida.
     * 
     * ESPECIFICACIÓN DE REQUERIMIENTOS:
     * 
     * Parámetros:
     * - nickname: Cadena con el nickname del jugador (o null/vacío si aún no se ha unido un rival).
     * - points: Número con los puntos de clasificación del jugador (o undefined).
     * - capturedPieces: Arreglo de instancias ChessPiece[] capturadas por este jugador.
     * 
     * Requerimientos Funcionales:
     * 1. Nombre: Mostrar en this.nameEl el nickname recibido; si es falsy, usar this.defaultName.
     * 2. Puntuación: Determinar los puntos numéricos (usar 1200 si no es numérico) y asignar a
     *    this.pointsEl el texto con formato: "<puntos> pts".
     * 3. Insignia de Tier: Actualizar this.tierBadge invocando:
     *    this.tierBadge.update(calculateTierFromPoints(puntos)).
     * 4. Piezas Capturadas:
     *    - Si capturedPieces es nulo o está vacío, vaciar this.capturedEl.textContent.
     *    - Si contiene piezas, transformar cada pieza a su glifo tipográfico mediante
     *      getPieceGlyph(p, this.capturedColorKey) y unirlas separadas por un espacio " ".
     */
    update(nickname, points, capturedPieces) {
        // TODO: Implementar la actualización de la tarjeta según la especificación de requerimientos de arriba.
    }

    getElement() {
        return this.element;
    }
}
