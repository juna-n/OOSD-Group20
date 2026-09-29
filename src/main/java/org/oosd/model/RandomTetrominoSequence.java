package org.oosd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
generates random types lazily and remembers them, so asking for the same
index twice always gives the same answer
synchronized because two players (and the AI threads looking at their
next piece) can ask for indexes at the same time
*/
public final class RandomTetrominoSequence implements TetrominoSequence {

    private static final TetrominoType[] TYPES = TetrominoType.values();

    private final List<TetrominoType> generated = new ArrayList<>();
    private final Random random;

    public RandomTetrominoSequence() {
        this(new Random());
    }

    //pass a seeded Random for a repeatable sequence
    public RandomTetrominoSequence(Random random) {
        this.random = random;
    }

    @Override
    public synchronized TetrominoType typeAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Negative piece index: " + index);
        }
        while (generated.size() <= index) {
            generated.add(TYPES[random.nextInt(TYPES.length)]);
        }
        return generated.get(index);
    }
}
