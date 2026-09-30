package org.oosd.controller.command;

import javafx.scene.input.KeyCode;
import org.oosd.controller.GameController;
import org.oosd.controller.GameSession;
import org.oosd.persistence.ConfigManager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
invoker in the Command pattern, a lookup table from key to Command

layout:
  single player  - A D or left/right move, W or up rotate, S or down drops
  two players    - player 1 uses W A S D, player 2 uses the arrow keys
  always         - P pause, M music, N sound effects
keys for AI / external players are simply never bound

held keys:
the operating system's key repeat is ignored, instead this class remembers
which keys are physically down and repeats them from the game loop
(update), so every held key keeps working on its own, holding left and
tapping rotate no longer stops the piece sliding
left and right of the same player share a repeat group, if both are held
only the one pressed most recently repeats, releasing it hands control
back to the other
*/
public final class KeyBindings {

    public static final KeyCode MUSIC_KEY = KeyCode.M;
    public static final KeyCode SOUND_KEY = KeyCode.N;
    public static final KeyCode PAUSE_KEY = KeyCode.P;

    //a frame gap longer than this (e.g. after a dialog) shouldn't fire a burst of repeats
    private static final double MAX_FRAME_SECONDS = 0.1;

    private static final Map<PieceAction, KeyCode> LETTER_LAYOUT = Map.of(
            PieceAction.MOVE_LEFT, KeyCode.A,
            PieceAction.MOVE_RIGHT, KeyCode.D,
            PieceAction.ROTATE, KeyCode.W,
            PieceAction.MOVE_DOWN, KeyCode.S);

    private static final Map<PieceAction, KeyCode> ARROW_LAYOUT = Map.of(
            PieceAction.MOVE_LEFT, KeyCode.LEFT,
            PieceAction.MOVE_RIGHT, KeyCode.RIGHT,
            PieceAction.ROTATE, KeyCode.UP,
            PieceAction.MOVE_DOWN, KeyCode.DOWN);

    //repeat is null for keys that act once per press (rotate, pause, toggles)
    //group is the name of the repeat group, null means the key repeats on its own
    private record Binding(Command command, RepeatRate repeat, String group) {
    }

    //one key currently held down
    private static final class HeldKey {
        private final Binding binding;
        private double timer;
        private boolean repeating;

        private HeldKey(Binding binding) {
            this.binding = binding;
        }

        //run the command as many times as the elapsed time allows
        private void advance(double elapsedSeconds) {
            RepeatRate rate = binding.repeat();
            timer += elapsedSeconds;
            double wait = repeating ? rate.intervalSeconds() : rate.delaySeconds();
            while (timer >= wait) {
                timer -= wait;
                repeating = true;
                wait = rate.intervalSeconds();
                binding.command().execute();
            }
        }

        //start the delay over, used when another key in the group takes over
        private void restart() {
            timer = 0;
            repeating = false;
        }
    }

    private final Map<KeyCode, Binding> bindings = new EnumMap<>(KeyCode.class);

    //insertion order is press order, so the last entry in a group is the newest
    private final Map<KeyCode, HeldKey> held = new LinkedHashMap<>();

    public static KeyBindings forSession(GameSession session, ConfigManager settings) {
        KeyBindings keys = new KeyBindings();
        keys.bind(PAUSE_KEY, new PauseCommand(session));
        keys.bind(MUSIC_KEY, ToggleSettingCommand.music(settings));
        keys.bind(SOUND_KEY, ToggleSettingCommand.soundEffects(settings));

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
        String sideways = "player" + target.playerNumber() + "-sideways";
        layout.forEach((action, key) -> {
            Command command = new PieceCommand(target, action);
            switch (action) {
                case MOVE_LEFT, MOVE_RIGHT -> keys.bindRepeating(key, command, RepeatRate.SHIFT, sideways);
                case MOVE_DOWN -> keys.bindRepeating(key, command, RepeatRate.SOFT_DROP, null);
                case ROTATE -> keys.bind(key, command);
            }
        });
    }

    //acts once per press, holding the key does nothing more
    public void bind(KeyCode key, Command command) {
        put(key, new Binding(command, null, null));
    }

    //acts on press and then keeps acting while held
    public void bindRepeating(KeyCode key, Command command, RepeatRate repeat, String group) {
        put(key, new Binding(command, Objects.requireNonNull(repeat, "repeat"), group));
    }

    private void put(KeyCode key, Binding binding) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(binding.command(), "command");
        bindings.put(key, binding);
    }

    public boolean isBound(KeyCode key) {
        return bindings.containsKey(key);
    }

    /*
    key went down, runs the command straight away
    the OS sends extra presses while a key is held, those are swallowed here
    (still returns true so the event is consumed) because update() does the repeating
    */
    public boolean press(KeyCode key) {
        Binding binding = bindings.get(key);
        if (binding == null) {
            return false;
        }
        if (held.containsKey(key)) {
            return true;
        }
        held.put(key, new HeldKey(binding));
        binding.command().execute();
        return true;
    }

    public boolean release(KeyCode key) {
        return held.remove(key) != null;
    }

    //forget every held key, used when the window loses focus and releases would never arrive
    public void releaseAll() {
        held.clear();
    }

    public boolean isHeld(KeyCode key) {
        return held.containsKey(key);
    }

    //called once per frame, repeats every held key that is due
    public void update(double elapsedSeconds) {
        double elapsed = Math.min(elapsedSeconds, MAX_FRAME_SECONDS);

        //work out which held key is in charge of each group (the newest one)
        Map<String, HeldKey> newestInGroup = new HashMap<>();
        List<HeldKey> ungrouped = new ArrayList<>();
        for (HeldKey key : held.values()) {
            Binding binding = key.binding;
            if (binding.repeat() == null) {
                continue;
            }
            if (binding.group() == null) {
                ungrouped.add(key);
            } else {
                HeldKey previous = newestInGroup.put(binding.group(), key);
                if (previous != null) {
                    previous.restart();
                }
            }
        }

        //copied first because a command could end the game and trigger releaseAll()
        List<HeldKey> due = new ArrayList<>(ungrouped);
        due.addAll(newestInGroup.values());
        due.forEach(key -> key.advance(elapsed));
    }

    //text for the side panel so each human player can see their own keys
    public static String controlsHint(int playerNumber, int playerCount) {
        if (playerCount == 1) {
            return "A D  or  \u2190 \u2192   move\nW  or  \u2191   rotate\nS  or  \u2193   down";
        }
        return playerNumber == 1
                ? "A D   move\nW   rotate\nS   down"
                : "\u2190 \u2192   move\n\u2191   rotate\n\u2193   down";
    }

    //text for the status bar at the top of the game screen
    public static String globalHint() {
        return PAUSE_KEY.getName() + "  pause    "
                + MUSIC_KEY.getName() + "  music    "
                + SOUND_KEY.getName() + "  sound    Esc  menu";
    }
}