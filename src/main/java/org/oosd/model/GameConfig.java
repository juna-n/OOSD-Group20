package org.oosd.model;

public record GameConfig(
        int fieldWidth,
        int fieldHeight,
        int startLevel,
        boolean music,
        boolean soundEffects,
        boolean aiPlay,
        boolean extendedMode) {

    public static final int MIN_WIDTH = 5;
    public static final int MAX_WIDTH = 15;
    public static final int MIN_HEIGHT = 15;
    public static final int MAX_HEIGHT = 30;
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 10;

    public static final GameConfig DEFAULT =
            new GameConfig(10, 20, 1, true, true, false, false);

    public GameConfig {
        if (fieldWidth < MIN_WIDTH || fieldWidth > MAX_WIDTH) {
            throw new IllegalArgumentException("Field width out of range: " + fieldWidth);
        }
        if (fieldHeight < MIN_HEIGHT || fieldHeight > MAX_HEIGHT) {
            throw new IllegalArgumentException("Field height out of range: " + fieldHeight);
        }
        if (startLevel < MIN_LEVEL || startLevel > MAX_LEVEL) {
            throw new IllegalArgumentException("Level out of range: " + startLevel);
        }
    }
}
