import { TierBadge } from "./TierBadge.js";
import { formatGameStatus } from "../services/chessRulesHelper.js";

/**
 * Componente de Vista de Lobby (Conexión, Acciones de Sala, Salas Activas y Ranking de Tiers).
 */
export class LobbyView {
    constructor(handlers = {}) {
        this.handlers = handlers;

        this.element = document.createElement("section");
        this.element.id = "lobby-view";

        this.hostInput = document.createElement("input");
        this.portInput = document.createElement("input");
        this.nickInput = document.createElement("input");
        this.connectBtn = document.createElement("button");

        this.createRoomBtn = document.createElement("button");
        this.joinCodeInput = document.createElement("input");
        this.joinRoomBtn = document.createElement("button");
        this.refreshBtn = document.createElement("button");

        this.roomsTableBody = document.createElement("tbody");
        this.leaderboardTableBody = document.createElement("tbody");

        this.build();
    }

    build() {
        this.element.appendChild(this.buildConnectionBar());
        this.element.appendChild(this.buildRoomActionsBar());
        this.element.appendChild(this.buildLobbyGrid());
    }

    buildConnectionBar() {
        const bar = document.createElement("div");
        bar.className = "connection-bar";

        const defaultHost = (typeof window !== "undefined" && window.location && window.location.hostname)
            ? window.location.hostname
            : "127.0.0.1";

        this.hostInput.type = "text";
        this.hostInput.id = "host-input";
        this.hostInput.value = defaultHost;
        this.hostInput.style.width = "130px";

        this.portInput.type = "number";
        this.portInput.id = "port-input";
        this.portInput.value = "10002";
        this.portInput.style.width = "85px";

        this.nickInput.type = "text";
        this.nickInput.id = "nick-input";
        this.nickInput.value = `WebPlayer_${Math.floor(Math.random() * 900 + 100)}`;
        this.nickInput.style.width = "140px";

        this.connectBtn.id = "connect-btn";
        this.connectBtn.className = "btn btn-success";
        this.connectBtn.textContent = "Conectar";
        this.connectBtn.addEventListener("click", () => {
            if (this.handlers.onConnect) {
                this.handlers.onConnect({
                    host: this.hostInput.value,
                    port: this.portInput.value,
                    nickname: this.nickInput.value
                });
            }
        });

        bar.appendChild(this.createInputGroup("Servidor:", "host-input", this.hostInput));
        bar.appendChild(this.createInputGroup("Puerto WS:", "port-input", this.portInput));
        bar.appendChild(this.createInputGroup("Nickname:", "nick-input", this.nickInput));
        bar.appendChild(this.connectBtn);

        return bar;
    }

    createInputGroup(labelText, inputId, inputEl) {
        const group = document.createElement("div");
        group.className = "input-group";

        const label = document.createElement("label");
        label.htmlFor = inputId;
        label.textContent = labelText;

        group.appendChild(label);
        group.appendChild(inputEl);
        return group;
    }

    buildRoomActionsBar() {
        const bar = document.createElement("div");
        bar.className = "room-actions-bar";

        this.createRoomBtn.id = "create-room-btn";
        this.createRoomBtn.className = "btn btn-primary";
        this.createRoomBtn.textContent = "Crear Nueva Sala";
        this.createRoomBtn.disabled = true;
        this.createRoomBtn.addEventListener("click", () => {
            if (this.handlers.onCreateRoom) {
                this.handlers.onCreateRoom(this.nickInput.value);
            }
        });

        const orSpan = document.createElement("span");
        orSpan.style.color = "var(--text-secondary)";
        orSpan.style.fontWeight = "bold";
        orSpan.textContent = "o";

        const joinGroup = document.createElement("div");
        joinGroup.className = "input-group";

        this.joinCodeInput.type = "text";
        this.joinCodeInput.id = "join-code-input";
        this.joinCodeInput.placeholder = "Código (ej. R-8K2NX)";
        this.joinCodeInput.style.width = "160px";
        this.joinCodeInput.style.textTransform = "uppercase";
        this.joinCodeInput.disabled = true;

        this.joinRoomBtn.id = "join-room-btn";
        this.joinRoomBtn.className = "btn btn-purple";
        this.joinRoomBtn.textContent = "Unirse a Sala";
        this.joinRoomBtn.disabled = true;
        this.joinRoomBtn.addEventListener("click", () => {
            if (this.handlers.onJoinRoom) {
                this.handlers.onJoinRoom(this.joinCodeInput.value, this.nickInput.value);
            }
        });

        joinGroup.appendChild(this.joinCodeInput);
        joinGroup.appendChild(this.joinRoomBtn);

        const spacer = document.createElement("div");
        spacer.style.flex = "1";

        this.refreshBtn.id = "refresh-btn";
        this.refreshBtn.className = "btn btn-secondary";
        this.refreshBtn.textContent = "Actualizar";
        this.refreshBtn.disabled = true;
        this.refreshBtn.addEventListener("click", () => {
            if (this.handlers.onRefresh) {
                this.handlers.onRefresh();
            }
        });

        bar.appendChild(this.createRoomBtn);
        bar.appendChild(orSpan);
        bar.appendChild(joinGroup);
        bar.appendChild(spacer);
        bar.appendChild(this.refreshBtn);

        return bar;
    }

    buildLobbyGrid() {
        const grid = document.createElement("div");
        grid.className = "lobby-grid";

        // Tarjeta de salas activas
        const roomsCard = document.createElement("div");
        roomsCard.className = "card";

        const roomsTitle = document.createElement("div");
        roomsTitle.className = "card-title";
        const roomsTitleSpan = document.createElement("span");
        roomsTitleSpan.textContent = "Salas de Juego Activas";
        roomsTitle.appendChild(roomsTitleSpan);

        const roomsTable = document.createElement("table");
        const roomsThead = document.createElement("thead");
        const roomsHeadRow = document.createElement("tr");
        ["Código", "Blancas", "Negras", "Espectadores", "Estado", "Acción"].forEach(colText => {
            const th = document.createElement("th");
            th.textContent = colText;
            roomsHeadRow.appendChild(th);
        });
        roomsThead.appendChild(roomsHeadRow);

        this.roomsTableBody.id = "rooms-table-body";
        this.renderEmptyRow(this.roomsTableBody, 6, "Conéctate al servidor para listar las salas.");

        roomsTable.appendChild(roomsThead);
        roomsTable.appendChild(this.roomsTableBody);
        roomsCard.appendChild(roomsTitle);
        roomsCard.appendChild(roomsTable);

        // Tarjeta de Leaderboard / Tiers
        const leaderCard = document.createElement("div");
        leaderCard.className = "card";

        const leaderTitle = document.createElement("div");
        leaderTitle.className = "card-title";
        const leaderTitleSpan = document.createElement("span");
        leaderTitleSpan.style.color = "#FFE082";
        leaderTitleSpan.textContent = "Tabla Global de Tiers & Clasificación";
        leaderTitle.appendChild(leaderTitleSpan);

        const leaderTable = document.createElement("table");
        const leaderThead = document.createElement("thead");
        const leaderHeadRow = document.createElement("tr");
        ["Pos", "Jugador", "Puntos", "Tier", "V / D / E"].forEach(colText => {
            const th = document.createElement("th");
            th.textContent = colText;
            leaderHeadRow.appendChild(th);
        });
        leaderThead.appendChild(leaderHeadRow);

        this.leaderboardTableBody.id = "leaderboard-table-body";
        this.renderEmptyRow(this.leaderboardTableBody, 5, "Conéctate al servidor para consultar el ranking.");

        leaderTable.appendChild(leaderThead);
        leaderTable.appendChild(this.leaderboardTableBody);
        leaderCard.appendChild(leaderTitle);
        leaderCard.appendChild(leaderTable);

        grid.appendChild(roomsCard);
        grid.appendChild(leaderCard);

        return grid;
    }

    renderEmptyRow(tbody, colSpan, message) {
        tbody.replaceChildren();
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = colSpan;
        td.style.textAlign = "center";
        td.style.color = "var(--text-secondary)";
        td.textContent = message;
        tr.appendChild(td);
        tbody.appendChild(tr);
    }

    setConnectingState() {
        this.connectBtn.disabled = true;
        this.connectBtn.textContent = "Conectando...";
    }

    setConnectionState(connected) {
        this.connectBtn.disabled = connected;
        this.connectBtn.textContent = connected ? "Conectado" : "Conectar";
        this.hostInput.disabled = connected;
        this.portInput.disabled = connected;
        this.createRoomBtn.disabled = !connected;
        this.joinCodeInput.disabled = !connected;
        this.joinRoomBtn.disabled = !connected;
        this.refreshBtn.disabled = !connected;
    }

    updateRooms(rooms) {
        if (!rooms || rooms.length === 0) {
            this.renderEmptyRow(this.roomsTableBody, 6, "No hay salas activas creadas. ¡Crea una para comenzar!");
            return;
        }

        this.roomsTableBody.replaceChildren();
        rooms.forEach(room => {
            const tr = document.createElement("tr");

            const tdCode = document.createElement("td");
            const codeStrong = document.createElement("strong");
            codeStrong.style.color = "#64B5F6";
            codeStrong.textContent = room.roomCode;
            tdCode.appendChild(codeStrong);

            const tdWhite = document.createElement("td");
            tdWhite.textContent = room.whitePlayer || "-";

            const tdBlack = document.createElement("td");
            tdBlack.textContent = room.blackPlayer || "-";

            const tdSpecs = document.createElement("td");
            tdSpecs.textContent = String(room.spectatorCount);

            const tdStatus = document.createElement("td");
            const statusSpan = document.createElement("span");
            statusSpan.style.fontSize = "0.8rem";
            statusSpan.style.color = "#E0E0E0";
            statusSpan.textContent = formatGameStatus(room.status);
            tdStatus.appendChild(statusSpan);

            const tdAction = document.createElement("td");
            const enterBtn = document.createElement("button");
            enterBtn.className = "btn btn-primary";
            enterBtn.style.padding = "4px 10px";
            enterBtn.style.fontSize = "0.75rem";
            enterBtn.textContent = "Entrar";
            enterBtn.addEventListener("click", () => {
                if (this.handlers.onJoinRoom) {
                    this.handlers.onJoinRoom(room.roomCode, this.nickInput.value);
                }
            });
            tdAction.appendChild(enterBtn);

            tr.appendChild(tdCode);
            tr.appendChild(tdWhite);
            tr.appendChild(tdBlack);
            tr.appendChild(tdSpecs);
            tr.appendChild(tdStatus);
            tr.appendChild(tdAction);

            this.roomsTableBody.appendChild(tr);
        });
    }

    updateLeaderboard(leaderboard) {
        if (!leaderboard || leaderboard.length === 0) {
            this.renderEmptyRow(this.leaderboardTableBody, 5, "Sin jugadores registrados aún.");
            return;
        }

        this.leaderboardTableBody.replaceChildren();
        leaderboard.forEach((player, index) => {
            const tr = document.createElement("tr");

            const tdPos = document.createElement("td");
            const posStrong = document.createElement("strong");
            posStrong.textContent = `#${index + 1}`;
            tdPos.appendChild(posStrong);

            const tdNick = document.createElement("td");
            const nickStrong = document.createElement("strong");
            nickStrong.textContent = player.nickname;
            tdNick.appendChild(nickStrong);

            const tdPoints = document.createElement("td");
            tdPoints.style.color = "var(--accent-amber)";
            tdPoints.style.fontWeight = "700";
            tdPoints.textContent = `${player.points} pts`;

            const tdTier = document.createElement("td");
            const badge = new TierBadge(player.tier);
            tdTier.appendChild(badge.getElement());

            const tdRecord = document.createElement("td");
            tdRecord.style.color = "var(--text-secondary)";
            tdRecord.textContent = `${player.wins} / ${player.losses} / ${player.draws}`;

            tr.appendChild(tdPos);
            tr.appendChild(tdNick);
            tr.appendChild(tdPoints);
            tr.appendChild(tdTier);
            tr.appendChild(tdRecord);

            this.leaderboardTableBody.appendChild(tr);
        });
    }

    getNickname() {
        return this.nickInput.value.trim() || "WebPlayer";
    }

    show() {
        this.element.style.display = "block";
    }

    hide() {
        this.element.style.display = "none";
    }

    getElement() {
        return this.element;
    }
}
