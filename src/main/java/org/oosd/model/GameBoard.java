package org.oosd.model;

public final class GameBoard {

    public static final int DEFAULT_ROWS = 20;
    public static final int DEFAULT_COLS = 10;

    private final int rows;
    private final int cols;
    private final TetrominoType[][] grid;

    public GameBoard(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new TetrominoType[rows][cols];
    }

    public GameBoard() {
        this(DEFAULT_ROWS, DEFAULT_COLS);
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    //the block at a slot, null if empty
    public TetrominoType blockAt(int row, int col) {
        return grid[row][col];
    }

    /*negative rows are allowed so a piece can spawn partly above the field
    and slide in. Those cells aren't checked against the grid
    */
    public boolean canPlace(Tetromino piece) {
        for (Cell cell : piece.cells()) {
            if (cell.col() < 0 || cell.col() >= cols || cell.row() >= rows) {
                return false;
            }
            if (cell.row() >= 0 && grid[cell.row()][cell.col()] != null) {
                return false;
            }
        }
        return true;
    }

    //freezes a piece into the grid, calls only after canPlace() has passed
    public void lock(Tetromino piece) {
        for (Cell cell : piece.cells()) {
            if (cell.row() >= 0) {
                grid[cell.row()][cell.col()] = piece.type();
            }
        }
    }

    /*
    Removing every full row and drops everything above it down
    return how many rows were cleared, so can award points
    (1, 2, 3 or 4 at once).
    */
    public int clearFullRows() {
        int cleared = 0;
        for (int row = rows - 1; row >= 0; row--) {
            if (isRowFull(row)) {
                collapseInto(row);
                cleared++;
                row++; //re-inspect this row as it now holds what was above it
            }
        }
        return cleared;
    }

    public boolean isRowFull(int row) {
        for (TetrominoType block : grid[row]) {
            if (block == null) {
                return false;
            }
        }
        return true;
    }

    private void collapseInto(int target) {
        for (int row = target; row > 0; row--) {
            System.arraycopy(grid[row - 1], 0, grid[row], 0, cols);
        }
        grid[0] = new TetrominoType[cols];
    }
}
