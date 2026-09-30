package org.oosd.controller;

import org.oosd.controller.player.PlayerFactory;
import org.oosd.model.GameConfig;
import org.oosd.model.GameState;
import org.oosd.model.RandomTetrominoSequence;
import org.oosd.model.TetrominoSequence;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/*
one round of play: one GameController in normal mode, two in extended mode
both games are built on the SAME TetrominoSequence, which is what
guarantees both players get identical pieces in the same order
the view talks to this one object instead of juggling each player itself
*/
public class GameSession {

    private final GameConfig config;
    private final List<GameController> controllers;

    public GameSession(GameConfig config, PlayerFactory factory) {
        this(config, factory, new RandomTetrominoSequence());
    }

    //tests pass a fixed sequence here to get predictable pieces
    public GameSession(GameConfig config, PlayerFactory factory, TetrominoSequence sequence) {
        this.config = Objects.requireNonNull(config, "config");
        Objects.requireNonNull(factory, "factory");
        Objects.requireNonNull(sequence, "sequence");

        this.controllers = IntStream.rangeClosed(1, config.playerCount())
                .mapToObj(playerNumber -> new GameController(
                        playerNumber,
                        new GameState(config, sequence),
                        factory.create(config.playerType(playerNumber))))
                .toList();
    }

    public GameConfig config() {
        return config;
    }

    public List<GameController> controllers() {
        return controllers;
    }

    public void start() {
        controllers.forEach(GameController::start);
    }

    public void update(double elapsedSeconds) {
        controllers.forEach(controller -> controller.update(elapsedSeconds));
    }

    // ---- pause applies to every field at once, like the demo ----

    public boolean isPaused() {
        return controllers.stream().anyMatch(controller -> controller.state().isPaused());
    }

    public void togglePause() {
        setPaused(!isPaused());
    }

    public void setPaused(boolean paused) {
        controllers.forEach(controller -> controller.state().setPaused(paused));
    }

    // ---- ending ----

    //the round is over once every field has topped out (or been stopped)
    public boolean isOver() {
        return controllers.stream().allMatch(controller -> controller.state().isGameOver());
    }

    public void endAll() {
        controllers.forEach(controller -> controller.state().endGame());
    }

    public void shutdown() {
        controllers.forEach(GameController::shutdown);
    }
}
