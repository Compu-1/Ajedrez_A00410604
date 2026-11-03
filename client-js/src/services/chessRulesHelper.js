/**
 * Utilidades de dominio de ajedrez y formato para el cliente Web (Vanilla JS).
 */

export const PIECE_GLYPHS = {
    white: {
        PIECEKING: "\u2654",
        PIECEQUEEN: "\u2655",
        PIECEROOK: "\u2656",
        PIECEBISHOP: "\u2657",
        PIECEKNIGHT: "\u2658",
        PIECEPAWN: "\u2659"
    },
    black: {
        PIECEKING: "\u265A",
        PIECEQUEEN: "\u265B",
        PIECEROOK: "\u265C",
        PIECEBISHOP: "\u265D",
        PIECEKNIGHT: "\u265E",
        PIECEPAWN: "\u265F"
    }
};

export function getPieceGlyph(piece, fallbackColor) {
    if (!piece || !piece.type || piece.type._name === "PIECEEMPTY") {
        return "";
    }
    const typeName = piece.type._name;
    const colorName = piece.color ? piece.color._name : fallbackColor;
    const isWhite = colorName === "COLORWHITE" || fallbackColor === "white";
    return isWhite ? (PIECE_GLYPHS.white[typeName] || "") : (PIECE_GLYPHS.black[typeName] || "");
}

export function calculateTierFromPoints(pts) {
    if (pts < 1200) return { _name: "TIERBRONZE" };
    if (pts < 1500) return { _name: "TIERSILVER" };
    if (pts < 1800) return { _name: "TIERGOLD" };
    if (pts < 2100) return { _name: "TIERPLATINUM" };
    return { _name: "TIERDIAMOND" };
}

export function formatGameStatus(status) {
    const sName = (status && status._name) ? status._name : String(status);
    switch (sName) {
        case "WAITINGFORPLAYERS": return "Esperando Rival";
        case "INPROGRESS": return "En Curso";
        case "CHECKWHITE": return "Jaque a Blancas";
        case "CHECKBLACK": return "Jaque a Negras";
        case "CHECKMATEWHITEWINS": return "Ganan Blancas (Mate)";
        case "CHECKMATEBLACKWINS": return "Ganan Negras (Mate)";
        case "STALEMATE": return "Tablas (Ahogado)";
        case "RESIGNEDWHITEWINS": return "Ganan Blancas (Abandono)";
        case "RESIGNEDBLACKWINS": return "Ganan Negras (Abandono)";
        case "ABORTED": return "Cancelada";
        default: return sName;
    }
}

/**
 * Calcula sugerencias de movimiento en el cliente para feedback visual inmediato.
 * La validación definitiva de jaque y legalidad autoritativa la realiza el Servidor Ice.
 */
export function calculateLocalCandidateMoves(board, r, c, myColor) {
    if (!board || !board[r] || !board[r][c]) return [];
    const piece = board[r][c];
    if (!piece.type || piece.type._name === "PIECEEMPTY") return [];

    const type = piece.type._name;
    const enemyColor = (myColor === "COLORWHITE") ? "COLORBLACK" : "COLORWHITE";
    const moves = [];

    function isInside(row, col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    function isEmpty(row, col) {
        const p = board[row][col];
        return !p || !p.type || p.type._name === "PIECEEMPTY";
    }

    function isEnemy(row, col) {
        const p = board[row][col];
        return p && p.color && p.color._name === enemyColor;
    }

    if (type === "PIECEPAWN") {
        const dir = (myColor === "COLORWHITE") ? -1 : 1;
        const startRow = (myColor === "COLORWHITE") ? 6 : 1;

        if (isInside(r + dir, c) && isEmpty(r + dir, c)) {
            moves.push({ row: r + dir, col: c });
            if (r === startRow && isInside(r + 2 * dir, c) && isEmpty(r + 2 * dir, c)) {
                moves.push({ row: r + 2 * dir, col: c });
            }
        }

        for (const dc of [-1, 1]) {
            if (isInside(r + dir, c + dc) && isEnemy(r + dir, c + dc)) {
                moves.push({ row: r + dir, col: c + dc });
            }
        }
    } else if (type === "PIECEKNIGHT") {
        const deltas = [[-2, -1], [-2, 1], [-1, -2], [-1, 2], [1, -2], [1, 2], [2, -1], [2, 1]];
        for (const [dr, dc] of deltas) {
            const nr = r + dr, nc = c + dc;
            if (isInside(nr, nc) && (isEmpty(nr, nc) || isEnemy(nr, nc))) {
                moves.push({ row: nr, col: nc });
            }
        }
    } else if (type === "PIECEBISHOP" || type === "PIECEROOK" || type === "PIECEQUEEN") {
        const dirs = [];
        if (type === "PIECEBISHOP" || type === "PIECEQUEEN") {
            dirs.push([-1, -1], [-1, 1], [1, -1], [1, 1]);
        }
        if (type === "PIECEROOK" || type === "PIECEQUEEN") {
            dirs.push([-1, 0], [1, 0], [0, -1], [0, 1]);
        }

        for (const [dr, dc] of dirs) {
            let nr = r + dr, nc = c + dc;
            while (isInside(nr, nc)) {
                if (isEmpty(nr, nc)) {
                    moves.push({ row: nr, col: nc });
                } else {
                    if (isEnemy(nr, nc)) {
                        moves.push({ row: nr, col: nc });
                    }
                    break;
                }
                nr += dr;
                nc += dc;
            }
        }
    } else if (type === "PIECEKING") {
        for (let dr = -1; dr <= 1; dr++) {
            for (let dc = -1; dc <= 1; dc++) {
                if (dr === 0 && dc === 0) continue;
                const nr = r + dr, nc = c + dc;
                if (isInside(nr, nc) && (isEmpty(nr, nc) || isEnemy(nr, nc))) {
                    moves.push({ row: nr, col: nc });
                }
            }
        }
    }

    return moves;
}
