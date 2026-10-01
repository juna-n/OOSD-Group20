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

    /*
    leftmost board column this piece actually covers
    col is the corner of the rotation box, which can be left of the real
    blocks, AI and external moves are expressed in this column instead
    */
    public int minCol() {
        return cells().stream().mapToInt(Cell::col).min().orElse(col);
    }

    /*
    the piece as a tight 0/1 grid with no empty border, e.g. T at rotation 0 is
    [[0,1,0],[1,1,1]], this is the shape format TetrisServer works with
    */
    public int[][] shapeMatrix() {
        List<Cell> cells = cells();
        int minRow = cells.stream().mapToInt(Cell::row).min().orElseThrow();
        int maxRow = cells.stream().mapToInt(Cell::row).max().orElseThrow();
        int minCol = cells.stream().mapToInt(Cell::col).min().orElseThrow();
        int maxCol = cells.stream().mapToInt(Cell::col).max().orElseThrow();

        int[][] matrix = new int[maxRow - minRow + 1][maxCol - minCol + 1];
        for (Cell cell : cells) {
            matrix[cell.row() - minRow][cell.col() - minCol] = 1;
        }
        return matrix;
    }
}