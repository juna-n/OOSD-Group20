package org.oosd.model;

import java.util.List;

//the 7 default tetromino shapes
public enum TetrominoType {

    I(4, 2, List.of(new Cell(1, 0), new Cell(1, 1), new Cell(1, 2), new Cell(1, 3))),
    O(2, 1, List.of(new Cell(0, 0), new Cell(0, 1), new Cell(1, 0), new Cell(1, 1))),
    T(3, 4, List.of(new Cell(0, 1), new Cell(1, 0), new Cell(1, 1), new Cell(1, 2))),
    S(3, 2, List.of(new Cell(0, 1), new Cell(0, 2), new Cell(1, 0), new Cell(1, 1))),
    Z(3, 2, List.of(new Cell(0, 0), new Cell(0, 1), new Cell(1, 1), new Cell(1, 2))),
    J(3, 4, List.of(new Cell(0, 0), new Cell(1, 0), new Cell(1, 1), new Cell(1, 2))),
    L(3, 4, List.of(new Cell(0, 2), new Cell(1, 0), new Cell(1, 1), new Cell(1, 2)));

    private final int boxSize;
    private final int distinctRotations;
    private final List<Cell> baseCells;

    TetrominoType(int boxSize, int distinctRotations, List<Cell> baseCells) {
        this.boxSize = boxSize;
        this.distinctRotations = distinctRotations;
        this.baseCells = baseCells;
    }

    //width and height of the square box this shape rotates inside
    public int boxSize() {
        return boxSize;
    }

    /*
    how many rotations give a different shape, O looks the same every way
    round and I, S, Z only have two looks, the AI skips the duplicates
    */
    public int distinctRotations() {
        return distinctRotations;
    }

    //the four cells of this shape at rotation 0, relative to the box origin
    public List<Cell> baseCells() {
        return baseCells;
    }
}