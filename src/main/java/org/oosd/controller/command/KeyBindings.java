package org.oosd.controller.command;

import javafx.scene.input.KeyCode;
import org.oosd.controller.GameController;
import org.oosd.controller.GameSession;
import org.oosd.persistence.ConfigManager;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
invoker in the Command pattern, a lookup table from key to Command
the layout matches the reference demo:
  single player  - ", ." or left/right move, L or up rotate, Space or down drops
  two players    - player 1 uses ", . L Space", player 2 uses the arrow keys
  always         - P pause, M music, S sound effects
keys for AI / external players are simply never bound
*/
public final class KeyBindings {

    private static final Map<PieceAction, KeyCode> LETTER_LAYOUT = Map.of(
            PieceAction.MOVE_LEFT, KeyCode.COMMA,
            PieceAction.MOVE_RIGHT, KeyCode.PERIOD,
            PieceAction.ROTATE, KeyCode.L,
            PieceAction.MOVE_DOWN, KeyCode.SPACE);

    private static final Map<PieceAction, KeyCode> ARROW_LAYOUT = Map.of(
            PieceAction.MOVE_LEFT, KeyCode.LEFT,
            PieceAction.MOVE_RIGHT, KeyCode.RIGHT,
            PieceAction.ROTATE, KeyCode.UP,
            PieceAction.MOVE_DOWN, KeyCode.DOWN);

    private final Map<KeyCode, Command> bindings = new EnumMap<>(KeyCode.class);

    public static KeyBindings forSession(GameSession session, ConfigManager settings) {
        KeyBindings keys = new KeyBindings();
        keys.bind(KeyCode.P, new PauseCommand(session));
        keys.bind(KeyCode.M, ToggleSettingCommand.music(settings));
        keys.bind(KeyCode.S, ToggleSettingCommand.soundEffects(settings));

        List<GameController> players = session.controllers();
        if (players.size() == 1) {
            bindPlayer(keys, players.getFirst(), LETTER_LAYOUT);
            bindPlayer(keys, players.getFirst(), ARROW_LAYOUT);
        } else {
            bindPlayer(keys, players.get(0), LETTER_LAYOUT);
            bindPlayer(keys, players.get(1), ARROW_LAYOUT);
        }
        return keys;
    }

    private static void bindPlayer(KeyBindings keys, GameController target, Map<PieceAction, KeyCode> layout) {
        if (!target.player().acceptsKeyboard()) {
            return;
        }
        layout.forEach((action, key) -> keys.bind(key, new PieceCommand(target, action)));
    }

    public void bind(KeyCode key, Command command) {
        bindings.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(command, "command"));
    }

    public boolean isBound(KeyCode key) {
        return bindings.containsKey(key);
    }

    //runs the command for this key, returns false if the key does nothing
    public boolean handle(KeyCode key) {
        Command command = bindings.get(key);
        if (command == null) {
            return false;
        }
        command.execute();
        return true;
    }

    //text for the side panel so each human player can see their own keys
    public static String controlsHint(int playerNumber, int playerCount) {
        if (playerCount == 1) {
            return ", .  or  \u2190 \u2192   move\nL  or  \u2191   rotate\nSpace  or  \u2193   down";
        }
        return playerNumber == 1
                ? ", .   move\nL   rotate\nSpace   down"
                : "\u2190 \u2192   move\n\u2191   rotate\n\u2193   down";
    }
}
