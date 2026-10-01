package org.oosd.controller.player;

import org.oosd.model.PlayerType;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 Factory pattern: the only place that knows which PlayerController class
 goes with which PlayerType
 GameSession just asks for "a player of this type" and gets back the
 interface, so it never names a concrete player class, and it also hides
 the setup automated players need (their own worker thread)
 not final so tests can substitute a factory that returns test doubles
*/
public class PlayerFactory {

    //how results get back onto the UI thread, Platform::runLater in the real game
    private final Executor uiExecutor;

    public PlayerFactory(Executor uiExecutor) {
        this.uiExecutor = Objects.requireNonNull(uiExecutor, "uiExecutor");
    }

    public PlayerController create(int playerNumber, PlayerType type) {
        Objects.requireNonNull(type, "type");
        return switch (type) {
            case HUMAN -> new HumanPlayer();
            case AI -> new AIPlayer(workerThread("ai-player-" + playerNumber), uiExecutor);
            //stand-in until ExternalPlayer arrives in section 5
            case EXTERNAL -> new HumanPlayer();
        };
    }

    /*
    one background thread per automated player
    daemon so a still-running worker can never stop the JVM from exiting
    */
    protected static ExecutorService workerThread(String name) {
        return Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, name);
            thread.setDaemon(true);
            return thread;
        });
    }
}