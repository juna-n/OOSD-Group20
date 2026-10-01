package org.oosd.model;

/*
 immutable snapshot of every setting on the configuration screen
 changing a setting means building a new GameConfig, the with...() helpers
 make that easy for the in-game M and S toggles
*/
public record GameConfig(
        int fieldWidth,
        int fieldHeight,
        int startLevel,
        boolean music,
        boolean soundEffects,
        boolean extendedMode,
        PlayerType playerOneType,
        PlayerType playerTwoType) {

    public static final int MIN_WIDTH = 5;
    public static final int MAX_WIDTH = 15;
    public static final int MIN_HEIGHT = 15;
    public static final int MAX_HEIGHT = 30;
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 10;

    public static final GameConfig DEFAULT = new GameConfig(
            10, 20, 1, true, true, false, PlayerType.HUMAN, PlayerType.HUMAN);

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
        //a config file written by an older build may not have these fields
        if (playerOneType == null) {
            playerOneType = PlayerType.HUMAN;
        }
        if (playerTwoType == null) {
            playerTwoType = PlayerType.HUMAN;
        }
    }

    //1 normally, 2 in extended mode
    public int playerCount() {
        return extendedMode ? 2 : 1;
    }

    //player number is 1 or 2, matching what the player sees on screen
    public PlayerType playerType(int playerNumber) {
        return switch (playerNumber) {
            case 1 -> playerOneType;
            case 2 -> playerTwoType;
            default -> throw new IllegalArgumentException("No such player: " + playerNumber);
        };
    }

    public GameConfig withMusic(boolean enabled) {
        return new GameConfig(fieldWidth, fieldHeight, startLevel, enabled, soundEffects,
                extendedMode, playerOneType, playerTwoType);
    }

    public GameConfig withSoundEffects(boolean enabled) {
        return new GameConfig(fieldWidth, fieldHeight, startLevel, music, enabled,
                extendedMode, playerOneType, playerTwoType);
    }

    //short description stored beside each high score, e.g. "10x20 L1 AI Single"
    public String summary(int playerNumber) {
        return String.format("%dx%d L%d %s %s",
                fieldWidth, fieldHeight, startLevel,
                playerType(playerNumber).label(),
                extendedMode ? "Double" : "Single");
    }
}