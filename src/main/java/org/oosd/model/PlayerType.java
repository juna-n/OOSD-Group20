package org.oosd.model;

//who is in control of a game field
public enum PlayerType {

    HUMAN("Human"),
    AI("AI"),
    EXTERNAL("External");

    private final String label;

    PlayerType(String label) {
        this.label = label;
    }

    //text shown in the config screen and in-game side panel
    public String label() {
        return label;
    }

    //true when the keyboard should be ignored for this player
    public boolean isAutomated() {
        return this != HUMAN;
    }
}
