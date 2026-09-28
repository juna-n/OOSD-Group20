package org.oosd.model;

/*
a frozen copy of one game at the moment a piece spawned
AI and external players work on this from a background thread, so they
never touch the live board the JavaFX thread is drawing
board is already a private copy, callers can simulate on a further copy
*/
public record BoardSnapshot(GameBoard board, Tetromino current, TetrominoType next, int pieceIndex) {

    public int width() {
        return board.cols();
    }

    public int height() {
        return board.rows();
    }

    //0 for empty, 1 for filled, the format TetrisServer expects
    public int[][] occupancy() {
        return board.toOccupancyMatrix();
    }
}
