package org.oosd.model;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/*
game logic for one field, knows nothing about JavaFX
subject side of the Observer pattern, interested parties register a
GameListener and are told when something happens
every method that changes state is expected to run on the JavaFX thread,
background players only ever read a snapshot()
*/
public class GameState {

    //column offsets tried when a rotation is blocked, in preference order
    private static final int[] WALL_KICKS = {0, -1, 1, -2, 2};

    private static final int ROWS_PER_LEVEL = 10;

    private final GameBoard board;
    private final TetrominoSequence sequence;
    private final int startLevel;

    //copy-on-write so a listener can safely unregister itself while being notified
    private final List<GameListener> listeners = new CopyOnWriteArrayList<>();

    private Tetromino current;
    private int pieceIndex;
    private int score;
    private int rowsCleared;
    private LineClear lastClear = LineClear.NONE;
    private boolean paused;
    private boolean gameOver;

    //single player convenience, gets its own random sequence
    public GameState(GameConfig config) {
        this(config, new RandomTetrominoSequence());
    }

    //two player mode passes the same sequence into both games
    public GameState(GameConfig config, TetrominoSequence sequence) {
        this.board = new GameBoard(config.fieldHeight(), config.fieldWidth());
        this.startLevel = config.startLevel();
        this.sequence = Objects.requireNonNull(sequence, "sequence");

        //first piece placed directly, nobody can be listening yet and an empty
        //board always has room (fields are at least 5 wide)
        this.pieceIndex = 0;
        this.current = Tetromino.spawn(sequence.typeAt(0), board.cols());
    }

    // ---- observer registration ----

    public void addListener(GameListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

    private void fire(GameEvent event) {
        for (GameListener listener : listeners) {
            listener.onGameEvent(event, this);
        }
    }

    // ---- queries ----

    public GameBoard board() {
        return board;
    }

    public Tetromino current() {
        return current;
    }

    public TetrominoType next() {
        return sequence.typeAt(pieceIndex + 1);
    }

    //position of the current piece in the shared sequence, goes up by one per piece
    public int pieceIndex() {
        return pieceIndex;
    }

    public int score() {
        return score;
    }

    public int rowsCleared() {
        return rowsCleared;
    }

    //result of the most recent lock, NONE if it cleared nothing
    public LineClear lastClear() {
        return lastClear;
    }

    public int startLevel() {
        return startLevel;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    //difficulty goes up one every ten rows cleared
    public int level() {
        return startLevel + rowsCleared / ROWS_PER_LEVEL;
    }

    //how long one row of automatic falling should take at each level
    public double secondsPerRow() {
        return Math.max(0.08, 0.80 - (level() - 1) * 0.07);
    }

    //true when the piece has somewhere to fall, so the UI knows to animate it
    public boolean canFall() {
        return isActive() && board.canPlace(current.movedBy(1, 0));
    }

    //frozen copy for AI and external players to think about off the UI thread
    public BoardSnapshot snapshot() {
        return new BoardSnapshot(board.copy(), current, next(), pieceIndex);
    }

    // ---- commands ----

    public void togglePause() {
        setPaused(!paused);
    }

    public void setPaused(boolean pause) {
        if (gameOver || paused == pause) {
            return;
        }
        paused = pause;
        fire(GameEvent.PAUSE_CHANGED);
    }

    //ends the game early, e.g. the player went back to the menu
    public void endGame() {
        if (gameOver) {
            return;
        }
        paused = false;
        gameOver = true;
        fire(GameEvent.GAME_OVER);
    }

    public boolean moveLeft() {
        return tryShift(-1);
    }

    public boolean moveRight() {
        return tryShift(1);
    }

    //rotates clockwise, nudging sideways if the rotation would clip a wall or other pieces
    public boolean rotate() {
        if (!isActive()) {
            return false;
        }
        Tetromino rotated = current.rotatedClockwise();
        for (int kick : WALL_KICKS) {
            Tetromino candidate = rotated.movedBy(0, kick);
            if (board.canPlace(candidate)) {
                current = candidate;
                fire(GameEvent.PIECE_ROTATED);
                return true;
            }
        }
        return false;
    }

    //one gravity tick: fall one row, or land if the way down is blocked
    public void step() {
        if (!isActive()) {
            return;
        }
        Tetromino down = current.movedBy(1, 0);
        if (board.canPlace(down)) {
            current = down;
        } else {
            lockAndClear();
        }
    }

    //player or AI pressed down, returns false when the piece landed instead of falling
    public boolean softDrop() {
        if (!isActive()) {
            return false;
        }
        boolean couldFall = board.canPlace(current.movedBy(1, 0));
        step();
        return couldFall;
    }

    //fall as far as possible and land immediately
    public void hardDrop() {
        if (!isActive()) {
            return;
        }
        while (board.canPlace(current.movedBy(1, 0))) {
            current = current.movedBy(1, 0);
        }
        lockAndClear();
    }

    // ---- internals ----

    private boolean isActive() {
        return !gameOver && !paused;
    }

    private boolean tryShift(int deltaCol) {
        if (!isActive()) {
            return false;
        }
        Tetromino candidate = current.movedBy(0, deltaCol);
        if (!board.canPlace(candidate)) {
            return false;
        }
        current = candidate;
        fire(GameEvent.PIECE_MOVED);
        return true;
    }

    private void lockAndClear() {
        board.lock(current);
        fire(GameEvent.PIECE_LOCKED);

        int levelBefore = level();
        lastClear = LineClear.of(board.clearFullRows());
        if (lastClear != LineClear.NONE) {
            rowsCleared += lastClear.rows();
            score += lastClear.points();
            fire(GameEvent.LINES_CLEARED);
            if (level() > levelBefore) {
                fire(GameEvent.LEVEL_UP);
            }
        }

        spawnNext();
    }

    private void spawnNext() {
        pieceIndex++;
        current = Tetromino.spawn(sequence.typeAt(pieceIndex), board.cols());
        if (board.canPlace(current)) {
            fire(GameEvent.PIECE_SPAWNED);
        } else {
            gameOver = true;
            fire(GameEvent.GAME_OVER);
        }
    }
}