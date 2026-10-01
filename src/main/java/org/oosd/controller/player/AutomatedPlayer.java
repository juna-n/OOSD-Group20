package org.oosd.controller.player;

import org.oosd.controller.GameController;
import org.oosd.model.BoardSnapshot;
import org.oosd.model.GameEvent;
import org.oosd.model.GameListener;
import org.oosd.model.GameState;
import org.oosd.model.Move;
import org.oosd.model.Tetromino;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/*
shared machinery for players that decide moves by themselves (AI, external server)

threading, step by step:
1. JavaFX thread: a new piece spawns (heard through GameListener), a frozen
   BoardSnapshot is taken and handed to this player's own worker thread
2. worker thread: decide() works out a Move, however long that takes, the
   game keeps running smoothly meanwhile because the UI thread isn't blocked
3. worker thread: the result is posted back with uiExecutor
   (Platform.runLater in the real game)
4. JavaFX thread: if the piece hasn't landed in the meantime the move
   becomes the plan, then onFrame() carries it out one visible step at a time
so the live GameState is only ever touched by the JavaFX thread, the
worker only ever sees its own private snapshot, no locks are needed

subclasses only fill in decide(), the rest of the algorithm is fixed here
*/
public abstract class AutomatedPlayer implements PlayerController {

    //seconds between each rotate / sideways step, slow enough to watch
    private static final double ALIGN_INTERVAL = 0.08;
    //seconds between each step down once the piece is in position
    private static final double DROP_INTERVAL = 0.04;
    //after a failed decision (e.g. server not running) ask again this often
    private static final double RETRY_INTERVAL = 1.0;

    private final ExecutorService worker;
    private final Executor uiExecutor;
    private final GameListener spawnListener = this::onGameEvent;

    // ---- only read or written on the JavaFX thread ----
    private GameController game;
    private Move plan;
    private boolean aligning;
    private boolean thinking;
    private double actionTimer;
    private double retryTimer = -1;   //negative means no retry is waiting

    //written on the JavaFX thread, read by the view, volatile to be safe either way
    private volatile String status = "";
    private volatile boolean stopped;

    protected AutomatedPlayer(ExecutorService worker, Executor uiExecutor) {
        this.worker = Objects.requireNonNull(worker, "worker");
        this.uiExecutor = Objects.requireNonNull(uiExecutor, "uiExecutor");
    }

    /*
    works out where the current piece should go, runs on the WORKER thread
    return empty if no move can be made right now, throw if something went
    wrong (the message is shown in the side panel), either way it is retried
    */
    protected abstract Optional<Move> decide(BoardSnapshot snapshot) throws Exception;

    //wording for the side panel when decide() throws, subclasses can make it friendlier
    protected String describeFailure(Exception failure) {
        return failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
    }

    @Override
    public void attach(GameController controller) {
        this.game = Objects.requireNonNull(controller, "controller");
        game.state().addListener(spawnListener);
        //the first piece is already on the board, no spawn event is coming for it
        requestDecision();
    }

    @Override
    public void shutdown() {
        stopped = true;
        worker.shutdownNow();
        if (game != null) {
            game.state().removeListener(spawnListener);
        }
    }

    @Override
    public String status() {
        return status;
    }

    //true while waiting for the worker, handy for tests and debugging
    public boolean isThinking() {
        return thinking;
    }

    public Optional<Move> currentPlan() {
        return Optional.ofNullable(plan);
    }

    // ---- 1. new piece: hand a snapshot to the worker ----

    private void onGameEvent(GameEvent event, GameState source) {
        if (event == GameEvent.PIECE_SPAWNED) {
            plan = null;
            requestDecision();
        }
    }

    private void requestDecision() {
        if (stopped || game.state().isGameOver()) {
            return;
        }
        BoardSnapshot snapshot = game.state().snapshot();
        thinking = true;
        retryTimer = -1;
        try {
            worker.execute(() -> think(snapshot));
        } catch (RejectedExecutionException e) {
            //worker already shut down, the game screen is closing
            thinking = false;
        }
    }

    // ---- 2 and 3. worker thread: decide, then post the answer back ----

    private void think(BoardSnapshot snapshot) {
        Optional<Move> move;
        Exception failure = null;
        try {
            move = decide(snapshot);
        } catch (Exception e) {
            move = Optional.empty();
            failure = e;
        }
        Optional<Move> result = move;
        Exception error = failure;
        uiExecutor.execute(() -> receive(snapshot.pieceIndex(), result, error));
    }

    // ---- 4. JavaFX thread: accept the answer if it is still relevant ----

    private void receive(int pieceIndex, Optional<Move> move, Exception failure) {
        if (stopped || pieceIndex != game.state().pieceIndex()) {
            //the piece this was worked out for has already landed, a newer request is on its way
            return;
        }
        thinking = false;
        status = failure == null ? "" : describeFailure(failure);

        if (move.isPresent()) {
            plan = move.get();
            aligning = true;
            actionTimer = 0;
        } else {
            //no answer this time: the piece keeps falling on its own while we wait to ask again
            retryTimer = RETRY_INTERVAL;
        }
    }

    // ---- carrying out the plan, one step per interval ----

    @Override
    public void onFrame(double elapsedSeconds) {
        if (thinking) {
            return;
        }
        if (plan == null) {
            countDownRetry(elapsedSeconds);
            return;
        }

        actionTimer += elapsedSeconds;
        //plan becomes null the moment the piece locks and the next one spawns
        while (plan != null && actionTimer >= currentInterval()) {
            actionTimer -= currentInterval();
            takeStep();
        }
    }

    private double currentInterval() {
        return aligning ? ALIGN_INTERVAL : DROP_INTERVAL;
    }

    private void countDownRetry(double elapsedSeconds) {
        if (retryTimer < 0) {
            return;
        }
        retryTimer -= elapsedSeconds;
        if (retryTimer <= 0) {
            requestDecision();
        }
    }

    /*
    rotate first, then slide, then drop
    if any step is blocked (the stack shifted while we were deciding) the
    rest of the alignment is abandoned and the piece is just dropped
    */
    private void takeStep() {
        if (aligning) {
            Tetromino piece = game.state().current();
            boolean moved;
            if (piece.rotation() != plan.targetRotation()) {
                moved = game.rotate();
            } else if (piece.minCol() > plan.column()) {
                moved = game.moveLeft();
            } else if (piece.minCol() < plan.column()) {
                moved = game.moveRight();
            } else {
                moved = false;   //in position
            }
            if (moved) {
                return;
            }
            aligning = false;
        }
        game.moveDown();
    }
}
