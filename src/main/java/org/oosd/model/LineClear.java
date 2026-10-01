package org.oosd.model;

import java.util.Arrays;

/*
points awarded for clearing rows with a single piece
1 line = 100, 2 = 300, 3 = 600, 4 = 1000 as required by the spec
*/
public enum LineClear {

    NONE(0, 0),
    SINGLE(1, 100),
    DOUBLE(2, 300),
    TRIPLE(3, 600),
    TETRIS(4, 1000);

    private final int rows;
    private final int points;

    LineClear(int rows, int points) {
        this.rows = rows;
        this.points = points;
    }

    public int rows() {
        return rows;
    }

    public int points() {
        return points;
    }

    //a tetromino is at most 4 tall, so anything outside 0..4 is a bug
    public static LineClear of(int rowsCleared) {
        return Arrays.stream(values())
                .filter(clear -> clear.rows == rowsCleared)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot clear " + rowsCleared + " rows with one piece"));
    }
}
