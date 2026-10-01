package org.oosd.controller.player;

import org.oosd.controller.GameController;
import org.oosd.model.PlayerType;

/*
whoever is in charge of one game field: a person at the keyboard, the
built-in AI, or TetrisServer
GameController talks to every kind through this interface, so adding a new
kind of player never means changing the controller (polymorphism)
*/
public interface PlayerController {

    PlayerType type();

    //only human players get key bindings
    default boolean acceptsKeyboard() {
        return !type().isAutomated();
    }

    //called once when the game starts, automated players begin watching the game here
    default void attach(GameController game) {
        //humans have nothing to set up
    }

    //called every animation frame on the JavaFX thread while the game is running
    default void onFrame(double elapsedSeconds) {
        //humans act through key presses instead
    }

    //stop any background threads, called when leaving the game screen
    default void shutdown() {
        //nothing to stop by default
    }

    //a warning for the side panel, e.g. server not running, empty when all is well
    default String status() {
        return "";
    }
}
