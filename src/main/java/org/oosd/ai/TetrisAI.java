package org.oosd.ai;

import org.oosd.model.BoardSnapshot;
import org.oosd.model.Cell;
import org.oosd.model.GameBoard;
import org.oosd.model.Move;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/*
picks where to put the current piece
1. list every placement the piece can actually reach: each distinct
   rotation, slid left and right from where it spawns (stopping at the
   first wall or block in the way), then dropped straight down
2. with lookahead on, also try every placement of the NEXT piece on top
   of each of those, and judge the pair by the better of their outcomes
3. return the placement of the current piece with the best score

pure calculation on a snapshot, it never touches the live game, so it is
safe to run on a background thread
*/
public final class TetrisAI {

    //a placement that leaves the next piece no room to spawn loses the game
    private static final double LOSING_SCORE = -1_000_000;

    //a finished placement: the move that gets there, and the board it leaves behind
    record Placement(Move move, GameBoard boardAfter, int linesCleared) {
    }

    private final BoardEvaluator evaluator;
    private final boolean lookahead;

    public TetrisAI() {
        this(new BoardEvaluator(), true);
    }

    public TetrisAI(BoardEvaluator evaluator, boolean lookahead) {
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator");
        this.lookahead = lookahead;
    }

    //empty only when the piece has nowhere to go at all (the game is about to end)
    public Optional<Move> findBestMove(BoardSnapshot snapshot) {
        TetrominoType current = snapshot.current().type();
        TetrominoType next = snapshot.next();

        Placement best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Placement placement : placements(snapshot.board(), current)) {
            double score = lookahead
                    ? scoreWithNext(placement, next)
                    : scoreAlone(placement, next);
            if (score > bestScore) {
                bestScore = score;
                best = placement;
            }
        }
        return Optional.ofNullable(best).map(Placement::move);
    }

    private double scoreAlone(Placement placement, TetrominoType next) {
        if (!canSpawn(placement.boardAfter(), next)) {
            return LOSING_SCORE;
        }
        return evaluator.evaluate(placement.boardAfter(), placement.linesCleared());
    }

    //the current placement is only as good as the best follow-up it allows
    private double scoreWithNext(Placement placement, TetrominoType next) {
        if (!canSpawn(placement.boardAfter(), next)) {
            return LOSING_SCORE;
        }
        double best = LOSING_SCORE;
        for (Placement followUp : placements(placement.boardAfter(), next)) {
            int lines = placement.linesCleared() + followUp.linesCleared();
            best = Math.max(best, evaluator.evaluate(followUp.boardAfter(), lines));
        }
        return best;
    }

    // ---- simulation ----

    //every placement of this piece type reachable from its spawn position
    List<Placement> placements(GameBoard board, TetrominoType type) {
        List<Placement> result = new ArrayList<>();
        Tetromino spawned = Tetromino.spawn(type, board.cols());

        for (int rotations = 0; rotations < type.distinctRotations(); rotations++) {
            Tetromino start = spawned;
            for (int turn = 0; turn < rotations; turn++) {
                start = start.rotatedClockwise();
            }
            if (!board.canPlace(start)) {
                continue;
            }

            //slide left from the start, including the start itself
            Tetromino piece = start;
            while (true) {
                addIfValid(result, board, piece, rotations);
                Tetromino left = piece.movedBy(0, -1);
                if (!board.canPlace(left)) {
                    break;
                }
                piece = left;
            }

            //slide right from the start
            piece = start;
            while (true) {
                Tetromino right = piece.movedBy(0, 1);
                if (!board.canPlace(right)) {
                    break;
                }
                piece = right;
                addIfValid(result, board, piece, rotations);
            }
        }
        return result;
    }

    //drop straight down, lock on a copy, clear rows, record it unless it locks above the top
    private void addIfValid(List<Placement> result, GameBoard board, Tetromino piece, int rotations) {
        Tetromino landed = piece;
        while (board.canPlace(landed.movedBy(1, 0))) {
            landed = landed.movedBy(1, 0);
        }
        for (Cell cell : landed.cells()) {
            if (cell.row() < 0) {
                return;
            }
        }

        GameBoard after = board.copy();
        after.lock(landed);
        int lines = after.clearFullRows();
        result.add(new Placement(new Move(rotations, landed.minCol()), after, lines));
    }

    private static boolean canSpawn(GameBoard board, TetrominoType type) {
        return board.canPlace(Tetromino.spawn(type, board.cols()));
    }
}
