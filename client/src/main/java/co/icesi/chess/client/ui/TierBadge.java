package co.icesi.chess.client.ui;

import ChessApp.TierLevel;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class TierBadge extends HBox {

    private static final String FONT_FAMILY = "-fx-font-family: 'Segoe UI', 'DejaVu Sans', Arial, sans-serif;";

    public TierBadge(TierLevel tier) {
        setAlignment(Pos.CENTER);

        String title;
        String bgColor;
        String textColor;

        if (tier == null) tier = TierLevel.TIERBRONZE;

        switch (tier) {
            case TIERDIAMOND:
                title = "DIAMANTE";
                bgColor = "#E1BEE7";
                textColor = "#4A148C";
                break;
            case TIERPLATINUM:
                title = "PLATINO";
                bgColor = "#B2EBF2";
                textColor = "#004D40";
                break;
            case TIERGOLD:
                title = "ORO";
                bgColor = "#FFF9C4";
                textColor = "#F57F17";
                break;
            case TIERSILVER:
                title = "PLATA";
                bgColor = "#ECEFF1";
                textColor = "#37474F";
                break;
            case TIERBRONZE:
            default:
                title = "BRONCE";
                bgColor = "#FFE0B2";
                textColor = "#E65100";
                break;
        }

        Label textLbl = new Label(title);
        textLbl.setStyle(FONT_FAMILY + String.format(" -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: %s;", textColor));

        getChildren().add(textLbl);
        setStyle(String.format(
                "-fx-background-color: %s; -fx-padding: 3 8 3 8; -fx-background-radius: 12px; -fx-border-color: %s; -fx-border-radius: 12px; -fx-border-width: 1px;",
                bgColor, textColor
        ));
    }
}
