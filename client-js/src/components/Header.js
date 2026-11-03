/**
 * Componente de Encabezado principal de la aplicación.
 */
export class Header {
    constructor() {
        this.element = document.createElement("header");
        this.statusBadge = document.createElement("span");
        this.build();
    }

    build() {
        const leftBox = document.createElement("div");

        const title = document.createElement("h1");
        title.textContent = "AJEDREZ DISTRIBUIDO ZEROC ICE";

        const subtitle = document.createElement("p");
        subtitle.textContent = "Computación en Internet I (09776 - TIC) - Frontend en JavaScript Puro (Vanilla JS)";

        leftBox.appendChild(title);
        leftBox.appendChild(subtitle);

        const rightBox = document.createElement("div");
        this.statusBadge.id = "global-status";
        this.statusBadge.className = "status-badge disconnected";
        this.statusBadge.textContent = "Desconectado";
        rightBox.appendChild(this.statusBadge);

        this.element.appendChild(leftBox);
        this.element.appendChild(rightBox);
    }

    setConnected(connected) {
        if (connected) {
            this.statusBadge.textContent = "Conectado";
            this.statusBadge.className = "status-badge connected";
        } else {
            this.statusBadge.textContent = "Desconectado";
            this.statusBadge.className = "status-badge disconnected";
        }
    }

    getElement() {
        return this.element;
    }
}
