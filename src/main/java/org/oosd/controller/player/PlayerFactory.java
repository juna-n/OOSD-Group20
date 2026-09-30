package org.oosd.controller.player;

import org.oosd.model.PlayerType;

import java.util.Objects;

/*
Factory pattern: the only place that knows which PlayerController class
goes with which PlayerType
GameSession just asks for "a player of this type" and gets back the
interface, so it never names a concrete player class
not final so tests can substitute a factory that returns test doubles
*/
public class PlayerFactory {

    public PlayerController create(PlayerType type) {
        Objects.requireNonNull(type, "type");
        return switch (type) {
            case HUMAN -> new HumanPlayer();
            //stand-ins until AIPlayer (section 4) and ExternalPlayer (section 5) exist
            case AI, EXTERNAL -> new HumanPlayer();
        };
    }
}
