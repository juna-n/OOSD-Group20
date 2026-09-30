package org.oosd.controller.command;

import org.oosd.controller.GameSession;

import java.util.Objects;

//P key, pauses or resumes every field together
public final class PauseCommand implements Command {

    private final GameSession receiver;

    public PauseCommand(GameSession receiver) {
        this.receiver = Objects.requireNonNull(receiver, "receiver");
    }

    @Override
    public void execute() {
        receiver.togglePause();
    }
}
