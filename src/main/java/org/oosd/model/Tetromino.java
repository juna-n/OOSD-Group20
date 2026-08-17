package org.oosd.model;

import java.util.ArrayList;
import java.util.List;

public record Tetromino(TetrominoType type, int rotation, int row, int col) {

    public Tetromino {
        rotation = Math.floorMod(rotation, 4);
    }

    //creates a piece sitting just above the top of the game field
    public static Tetromino spawn(TetrominoType type, int fieldCols) {
        return new Tetromino(type, 0, 0, (fieldCols - type.boxSize()) / 2);
    }

    public List<Cell> cells() {
        int n = type.boxSize();
        List<Cell> result = new ArrayList<>(4);

        for (Cell base : type.baseCells()) {   // enhanced for loop
            int r = base.row();
            int c = base.col();
            for (int turn = 0; turn < rotation; turn++) {
                int rotatedRow = c;
                int rotatedCol = n - 1 - r;
                r = rotatedRow;
                c = rotatedCol;
            }
            result.add(new Cell(row + r, col + c));
        }
        return result;
    }

    public Tetromino movedBy(int deltaRow, int deltaCol) {
        return new Tetromino(type, rotation, row + deltaRow, col + deltaCol);
    }

    public Tetromino rotatedClockwise() {
        return new Tetromino(type, rotation + 1, row, col);
    }
}
