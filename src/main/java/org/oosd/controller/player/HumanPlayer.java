package org.oosd.controller.player;

import org.oosd.model.PlayerType;

//a person at the keyboard, all input arrives through KeyBindings so there is nothing to do here
public final class HumanPlayer implements PlayerController {

    @Override
    public PlayerType type() {
        return PlayerType.HUMAN;
    }
}
