package org.oosd.controller.player;

import org.oosd.ai.TetrisAI;
import org.oosd.model.BoardSnapshot;
import org.oosd.model.Move;
import org.oosd.model.PlayerType;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

//the built-in computer player, AutomatedPlayer handles the threading, TetrisAI does the thinking
public final class AIPlayer extends AutomatedPlayer {

    private final TetrisAI ai;

    public AIPlayer(ExecutorService worker, Executor uiExecutor) {
        this(worker, uiExecutor, new TetrisAI());
    }

    public AIPlayer(ExecutorService worker, Executor uiExecutor, TetrisAI ai) {
        super(worker, uiExecutor);
        this.ai = Objects.requireNonNull(ai, "ai");
    }

    @Override
    public PlayerType type() {
        return PlayerType.AI;
    }

    @Override
    protected Optional<Move> decide(BoardSnapshot snapshot) {
        return ai.findBestMove(snapshot);
    }
}
