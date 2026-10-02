package org.oosd.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {

    //STUB: always returns an I piece instead of a random one, so every game is predictable
    private static final TetrominoSequence ONLY_I_PIECES = index -> TetrominoType.I;

    //8 columns wide, so two flat I pieces fill exactly one row
    private static final GameConfig NARROW = new GameConfig(8, 15, 1, false, false, false,
            PlayerType.HUMAN, PlayerType.HUMAN);

    private GameState game;

    @BeforeEach
    void setUp() {
        game = new GameState(NARROW, ONLY_I_PIECES);
    }

    private void clearOneRow() {
        while (game.moveLeft()) {
            //slide to the left wall
        }
        game.hardDrop();
        while (game.moveRight()) {
            //slide to the right wall
        }
        game.hardDrop();
    }

    @Test
    void moveLeftStopsAtTheWall() {
        while (game.moveLeft()) {
            //slide to the left wall
        }
        assertEquals(0, game.current().minCol());
        assertFalse(game.moveLeft());
    }

    @Test
    void rotateTurnsThePiece() {
        assertTrue(game.rotate());
        assertEquals(1, game.current().rotation());
    }

    @Test
    void clearingOneRowScores100() {
        clearOneRow();
        assertEquals(100, game.score());
        assertEquals(1, game.rowsCleared());
    }

    @Test
    void tenRowsRaiseTheLevel() {
        for (int i = 0; i < 10; i++) {
            clearOneRow();
        }
        assertEquals(2, game.level());
    }

    @Test
    void gameEndsWhenPiecesReachTheTop() {
        for (int i = 0; i < 50 && !game.isGameOver(); i++) {
            game.hardDrop();
        }
        assertTrue(game.isGameOver());
    }
}
