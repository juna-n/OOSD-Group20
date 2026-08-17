package org.oosd.model;

import java.util.List;

//the 7 default tetromino shapes
public enum TetrominoType {

    I(4, List.of(new Cell(1, 0), new Cell(1, 1), new Cell(1, 2), new Cell(1, 3))),
    O(2, List.of(new Cell(0, 0), new Cell(0, 1), new Cell(1, 0), new Cell(1, 1))),
    T(3, List.of(new Cell(0, 1), new Cell(1, 0), new Cell(1, 1), new Cell(1, 2))),
    S(3, List.of(new Cell(0, 1), new Cell(0, 2), new Cell(1, 0), new Cell(1, 1))),
    Z(3, List.of(new Cell(0, 0), new Cell(0, 1), new Cell(1, 1), new Cell(1, 2))),
    J(3, List.of(new Cell(0, 0), new Cell(1, 0), new Cell(1, 1), new Cell(1, 2))),
    L(3, List.of(new Cell(0, 2), new Cell(1, 0), new Cell(1, 1), new Cell(1, 2)));

    private final int boxSize;
    private final List<Cell> baseCells;

    TetrominoType(int boxSize, List<Cell> baseCells) {
        this.boxSize = boxSize;
        this.baseCells = baseCells;
    }

    //width and height of the square box this shape rotates inside
    public int boxSize() {
        return boxSize;
    }

    //the four cells of this shape at rotation 0, relative to the box origin
    public List<Cell> baseCells() {
        return baseCells;
    }
}
