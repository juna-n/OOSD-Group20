package org.oosd.model;

import java.util.List;
import java.util.Random;

//game logic
public class GameState {

    private static final List<TetrominoType> TYPES = List.of(TetrominoType.values());

    //column offsets tried when a rotation is blocked, in preference order
    private static final int[] WALL_KICKS = {0, -1, 1, -2, 2};

    private final GameBoard board;
    private final Random random = new Random();
    private final int startLevel;

    private Tetromino current;
    private TetrominoType next;
    private int score;
    private int rowsCleared;
    private boolean paused;
    private boolean gameOver;

    public GameState(GameConfig config) {
        this.board = new GameBoard(config.fieldHeight(), config.fieldWidth());
        this.startLevel = config.startLevel();
        this.next = randomType();
        spawnNext();
    }

    public GameBoard board() {
        return board;
    }

    public Tetromino current() {
        return current;
    }

    public TetrominoType next() {
        return next;
    }

    public int score() {
        return score;
    }

    public int rowsCleared() {
        return rowsCleared;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    //difficulty goes up one every ten rows cleared
    public int level() {
        return startLevel + rowsCleared / 10;
    }

    //how long one row of automatic falling should take at each level
    public double secondsPerRow() {
        return Math.max(0.08, 0.80 - (level() - 1) * 0.07);
    }

    //true when the piece has somewhere to fall, so the UI knows to animate it
    public boolean canFall() {
        return !gameOver && !paused && board.canPlace(current.movedBy(1, 0));
    }

    public void togglePause() {
        if (!gameOver) {
            paused = !paused;
        }
    }

    public void moveLeft() {
        tryMove(current.movedBy(0, -1));
    }

    public void moveRight() {
        tryMove(current.movedBy(0, 1));
    }

    //rotates clockwise, nudging sideways if the rotation would clip a wall or other pieces
    public void rotate() {
        if (gameOver || paused) {
            return;
        }
        Tetromino rotated = current.rotatedClockwise();
        for (int kick : WALL_KICKS) {
            Tetromino candidate = rotated.movedBy(0, kick);
            if (board.canPlace(candidate)) {
                current = candidate;
                return;
            }
        }
    }

    //one gravity tick: fall one row, or land if the way down is blocked
    public void step() {
        if (gameOver || paused) {
            return;
        }
        Tetromino down = current.movedBy(1, 0);
        if (board.canPlace(down)) {
            current = down;
        } else {
            lockAndClear();
        }
    }

    //player pressed down: the same as gravity tick, just on command
    public void softDrop() {
        step();
    }

    //player pressed space: fall as far as possible and land immediately
    public void hardDrop() {
        if (gameOver || paused) {
            return;
        }
        while (board.canPlace(current.movedBy(1, 0))) {
            current = current.movedBy(1, 0);
        }
        lockAndClear();
    }

    private boolean tryMove(Tetromino candidate) {
        if (gameOver || paused) {
            return false;
        }
        if (board.canPlace(candidate)) {
            current = candidate;
            return true;
        }
        return false;
    }

    private void lockAndClear() {
        board.lock(current);
        int cleared = board.clearFullRows();
        rowsCleared += cleared;

        //clearing several rows at once is worth more than clearing them one at a time
        score += switch (cleared) {
            case 1 -> 100 * level();
            case 2 -> 300 * level();
            case 3 -> 500 * level();
            case 4 -> 800 * level();
            default -> 0;
        };

        spawnNext();
    }

    private void spawnNext() {
        current = Tetromino.spawn(next, board.cols());
        next = randomType();
        if (!board.canPlace(current)) {
            gameOver = true;
        }
    }

    private TetrominoType randomType() {
        return TYPES.get(random.nextInt(TYPES.size()));
    }
}
