package org.oosd.audio;

//every sound effect in the game, each tied to its file in src/main/resources/audio/
public enum Sound {

    MOVE_TURN("move-turn.wav"),
    ERASE_LINE("erase-line.wav"),
    LEVEL_UP("level-up.wav"),
    GAME_FINISH("game-finish.wav");

    private final String fileName;

    Sound(String fileName) {
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }

    //classpath location, e.g. /audio/move-turn.wav
    public String resourcePath() {
        return AudioManager.AUDIO_FOLDER + fileName;
    }
}
