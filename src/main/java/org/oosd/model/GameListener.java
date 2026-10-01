package org.oosd.model;

/*
observer side of the Observer pattern
the model only knows this interface, so views, sound and players can react
to the game without GameState depending on any of them
*/
@FunctionalInterface
public interface GameListener {
    void onGameEvent(GameEvent event, GameState source);
}
