package org.oosd.audio;

/*
anything that can play a sound effect
GameSoundEffects depends on this small interface rather than on
AudioManager itself (Dependency Inversion), so it can be tested with a
mock that just records which sounds were asked for, no speakers needed
*/
@FunctionalInterface
public interface SoundPlayer {
    void play(Sound sound);
}
