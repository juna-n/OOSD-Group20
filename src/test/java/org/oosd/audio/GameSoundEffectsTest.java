package org.oosd.audio;

import org.junit.jupiter.api.Test;
import org.oosd.model.GameConfig;
import org.oosd.model.GameState;
import org.oosd.model.TetrominoType;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class GameSoundEffectsTest {

    //MOCK: records which sounds were requested, no speakers or JavaFX needed
    private final SoundPlayer speaker = mock(SoundPlayer.class);
    private final GameState game = new GameState(GameConfig.DEFAULT, index -> TetrominoType.T);

    @Test
    void humanMovesPlayTheClick() {
        game.addListener(new GameSoundEffects(speaker, true));
        game.moveLeft();
        verify(speaker).play(Sound.MOVE_TURN);
    }

    @Test
    void aiMovesAreSilent() {
        game.addListener(new GameSoundEffects(speaker, false));
        game.moveLeft();
        verify(speaker, never()).play(Sound.MOVE_TURN);
    }
}
