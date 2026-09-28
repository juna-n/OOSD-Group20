package org.oosd.ui;

import org.oosd.model.TetrominoType;
import javafx.scene.paint.Color;

//all tetromino colours
public final class BlockPalette {

    private BlockPalette() {

    }

    public static Color colorOf(TetrominoType type) {
        return switch (type) {
            case I -> Color.web("#31c7ef");
            case O -> Color.web("#f7d308");
            case T -> Color.web("#ad4d9c");
            case S -> Color.web("#42b642");
            case Z -> Color.web("#ef2029");
            case J -> Color.web("#5a65ad");
            case L -> Color.web("#ef7921");
        };
    }
}
