package org.oosd.controller.command;

import org.oosd.controller.GameController;

import java.util.Objects;

//moves one specific player's piece, receiver is that player's GameController
public final class PieceCommand implements Command {

    private final GameController receiver;
    private final PieceAction action;

    public PieceCommand(GameController receiver, PieceAction action) {
        this.receiver = Objects.requireNonNull(receiver, "receiver");
        this.action = Objects.requireNonNull(action, "action");
    }

    @Override
    public void execute() {
        action.applyTo(receiver);
    }

    public PieceAction action() {
        return action;
    }
}
