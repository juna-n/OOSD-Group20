package org.oosd.model;

import java.util.List;

/*
an endless, indexable list of piece types
in two player mode both GameStates share one sequence and each keeps its
own index into it, so both players always receive the same pieces in the
same order no matter how fast either of them plays
*/
@FunctionalInterface
public interface TetrominoSequence {

    //the type of the piece at this position, index 0 is the first piece of the game
    TetrominoType typeAt(int index);

    //repeats the given types forever, used by tests to get predictable pieces
    static TetrominoSequence cycling(TetrominoType... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException("At least one type is required");
        }
        List<TetrominoType> fixed = List.of(types);
        return index -> fixed.get(index % fixed.size());
    }
}
