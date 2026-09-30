package org.oosd.controller;

import org.oosd.controller.player.PlayerController;
import org.oosd.model.GameState;
import org.oosd.model.PlayerType;

import java.util.Objects;

/*
the C in MVC for one game field
- turns time into gravity (the model has no clock)
- is the single entry point for moves, whether they come from a key
  press or from an AI / external player
- lets the player controller act once per frame
no JavaFX in here, so it can be unit tested without starting the UI
*/
public class GameController {

    //a long pause (dialog, debugger, window drag) shouldn't drop the piece many rows at once
    private static final double MAX_FRAME_SECONDS = 0.25;

    private final int playerNumber;
    private final GameState state;
    private final PlayerController player;

    //fraction of the way to the next row, also used by the view for smooth falling
    private double fallProgress;

    public GameController(int playerNumber, GameState state, PlayerController player) {
        this.playerNumber = playerNumber;
        this.state = Objects.requireNonNull(state, "state");
        this.player = Objects.requireNonNull(player, "player");
    }

    public void start() {
        player.attach(this);
    }

    public void shutdown() {
        player.shutdown();
    }

    // ---- queries ----

    public int playerNumber() {
        return playerNumber;
    }

    public GameState state() {
        return state;
    }

    public PlayerController player() {
        return player;
    }

    public PlayerType playerType() {
        return player.type();
    }

    public double fallProgress() {
        return fallProgress;
    }

    // ---- moves, used by key commands and automated players alike ----

    public boolean moveLeft() {
        return state.moveLeft();
    }

    public boolean moveRight() {
        return state.moveRight();
    }

    public boolean rotate() {
        return state.rotate();
    }

    //one row down on demand, restarts the gravity timer so the piece doesn't double-drop
    public boolean moveDown() {
        boolean fell = state.softDrop();
        fallProgress = 0;
        return fell;
    }

    // ---- per-frame update ----

    public void update(double elapsedSeconds) {
        if (state.isGameOver() || state.isPaused()) {
            return;
        }
        double elapsed = Math.min(elapsedSeconds, MAX_FRAME_SECONDS);

        fallProgress += elapsed / state.secondsPerRow();
        while (fallProgress >= 1.0 && !state.isGameOver()) {
            fallProgress -= 1.0;
            state.step();
        }

        if (!state.isGameOver()) {
            player.onFrame(elapsed);
        }
    }
}
