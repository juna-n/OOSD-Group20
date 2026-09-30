package org.oosd.ai;

import org.oosd.model.GameBoard;

import java.util.Objects;

/*
gives a board a single score, higher is better
  aggregate height - sum of every column's height (lower is better)
  lines cleared    - rows completed by the placement (more is better)
  holes            - empty cells with a block somewhere above them (fewer is better)
  bumpiness        - total height difference between neighbouring columns (flatter is better)
*/
public final class BoardEvaluator {

    public record Weights(double height, double lines, double holes, double bumpiness) {
        public static final Weights TUNED = new Weights(-0.510066, 0.760666, -0.35663, -0.184483);
    }

    private final Weights weights;

    public BoardEvaluator() {
        this(Weights.TUNED);
    }

    public BoardEvaluator(Weights weights) {
        this.weights = Objects.requireNonNull(weights, "weights");
    }

    //board is the field after the piece locked and full rows were removed
    public double evaluate(GameBoard board, int linesCleared) {
        int[] heights = columnHeights(board);
        return weights.height() * aggregateHeight(heights)
                + weights.lines() * linesCleared
                + weights.holes() * holes(board)
                + weights.bumpiness() * bumpiness(heights);
    }

    // ---- features, package-private so they can be unit tested one by one ----

    //height of each column, 0 for empty, board.rows() for full to the top
    static int[] columnHeights(GameBoard board) {
        int[] heights = new int[board.cols()];
        for (int col = 0; col < board.cols(); col++) {
            for (int row = 0; row < board.rows(); row++) {
                if (board.blockAt(row, col) != null) {
                    heights[col] = board.rows() - row;
                    break;
                }
            }
        }
        return heights;
    }

    static int aggregateHeight(int[] heights) {
        int total = 0;
        for (int height : heights) {
            total += height;
        }
        return total;
    }

    static int holes(GameBoard board) {
        int holes = 0;
        for (int col = 0; col < board.cols(); col++) {
            boolean roofFound = false;
            for (int row = 0; row < board.rows(); row++) {
                if (board.blockAt(row, col) != null) {
                    roofFound = true;
                } else if (roofFound) {
                    holes++;
                }
            }
        }
        return holes;
    }

    static int bumpiness(int[] heights) {
        int total = 0;
        for (int col = 0; col < heights.length - 1; col++) {
            total += Math.abs(heights[col] - heights[col + 1]);
        }
        return total;
    }
}
