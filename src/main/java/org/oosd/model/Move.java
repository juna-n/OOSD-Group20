package org.oosd.model;

/*
where an automated player wants the current piece to end up
rotations - clockwise turns from the orientation the piece spawned in
column    - the leftmost board column the piece should cover (Tetromino.minCol)
using minCol rather than the rotation box corner means the same Move works
for our own AI and for TetrisServer's opRotate / opX answer
*/
public record Move(int rotations, int column) {

    public Move {
        if (rotations < 0) {
            throw new IllegalArgumentException("Rotations cannot be negative: " + rotations);
        }
        if (column < 0) {
            throw new IllegalArgumentException("Column cannot be negative: " + column);
        }
    }

    //the rotation value (0 to 3) the piece should have once the move is done
    public int targetRotation() {
        return rotations % 4;
    }
}
