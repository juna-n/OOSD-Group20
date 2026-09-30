package org.oosd.controller.command;

import org.oosd.controller.GameController;

import java.util.function.Consumer;

//the four things a player can do to the falling piece, each tied to the controller method that does it
public enum PieceAction {

    MOVE_LEFT(GameController::moveLeft),
    MOVE_RIGHT(GameController::moveRight),
    ROTATE(GameController::rotate),
    MOVE_DOWN(GameController::moveDown);

    private final Consumer<GameController> operation;

    PieceAction(Consumer<GameController> operation) {
        this.operation = operation;
    }

    public void applyTo(GameController controller) {
        operation.accept(controller);
    }
}
