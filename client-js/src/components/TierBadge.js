/**
 * Componente reutilizable de Insignia de Tier (Bronce, Plata, Oro, Platino, Diamante).
 */
export class TierBadge {
    constructor(tier) {
        this.element = document.createElement("span");
        this.update(tier);
    }

    update(tier) {
        const tierName = (tier && tier._name) ? tier._name : "TIERBRONZE";
        switch (tierName) {
            case "TIERDIAMOND":
                this.element.className = "tier-badge tier-diamond";
                this.element.textContent = "DIAMANTE";
                break;
            case "TIERPLATINUM":
                this.element.className = "tier-badge tier-platinum";
                this.element.textContent = "PLATINO";
                break;
            case "TIERGOLD":
                this.element.className = "tier-badge tier-gold";
                this.element.textContent = "ORO";
                break;
            case "TIERSILVER":
                this.element.className = "tier-badge tier-silver";
                this.element.textContent = "PLATA";
                break;
            case "TIERBRONZE":
            default:
                this.element.className = "tier-badge tier-bronze";
                this.element.textContent = "BRONCE";
                break;
        }
    }

    getElement() {
        return this.element;
    }
}
