package org.oosd.audio;

import org.oosd.model.GameEvent;
import org.oosd.model.GameListener;
import org.oosd.model.GameState;

import java.util.Objects;
import java.util.Optional;

/*
observer that turns game events into sound effects, one per game field
the model never knows sound exists, it just reports events

the move/turn click is only for human players, like the reference demo,
otherwise an AI sliding pieces around would click non-stop
*/
public final class GameSoundEffects implements GameListener {

    private final SoundPlayer player;
    private final boolean movementSounds;

    public GameSoundEffects(SoundPlayer player, boolean movementSounds) {
        this.player = Objects.requireNonNull(player, "player");
        this.movementSounds = movementSounds;
    }

    @Override
    public void onGameEvent(GameEvent event, GameState source) {
        soundFor(event).ifPresent(player::play);
    }

    /*
    no default branch on purpose: if a new GameEvent is ever added the
    compiler forces a decision about its sound here
    */
    Optional<Sound> soundFor(GameEvent event) {
        return switch (event) {
            case PIECE_MOVED, PIECE_ROTATED -> movementSounds ? Optional.of(Sound.MOVE_TURN) : Optional.empty();
            case LINES_CLEARED -> Optional.of(Sound.ERASE_LINE);
            case LEVEL_UP -> Optional.of(Sound.LEVEL_UP);
            case GAME_OVER -> Optional.of(Sound.GAME_FINISH);
            case PIECE_LOCKED, PIECE_SPAWNED, PAUSE_CHANGED -> Optional.empty();
        };
    }
}
