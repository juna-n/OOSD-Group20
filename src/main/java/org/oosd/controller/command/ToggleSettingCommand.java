package org.oosd.controller.command;

import org.oosd.model.GameConfig;
import org.oosd.persistence.ConfigManager;

import java.util.Objects;
import java.util.function.UnaryOperator;

/*
M and S keys, flips a setting in the saved configuration
going through ConfigManager means the change is written to the JSON file
and every ConfigManager listener (status bar, audio) hears about it at once
*/
public final class ToggleSettingCommand implements Command {

    private final ConfigManager receiver;
    private final UnaryOperator<GameConfig> change;

    private ToggleSettingCommand(ConfigManager receiver, UnaryOperator<GameConfig> change) {
        this.receiver = Objects.requireNonNull(receiver, "receiver");
        this.change = change;
    }

    public static ToggleSettingCommand music(ConfigManager receiver) {
        return new ToggleSettingCommand(receiver, config -> config.withMusic(!config.music()));
    }

    public static ToggleSettingCommand soundEffects(ConfigManager receiver) {
        return new ToggleSettingCommand(receiver, config -> config.withSoundEffects(!config.soundEffects()));
    }

    @Override
    public void execute() {
        receiver.update(change.apply(receiver.current()));
    }
}
