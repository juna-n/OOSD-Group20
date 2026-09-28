package org.oosd.model;

import java.util.Objects;

public final class ConfigStore {

    private static GameConfig current = GameConfig.DEFAULT;

    private ConfigStore() {
        //utility class
    }

    public static GameConfig current() {
        return current;
    }

    public static void update(GameConfig config) {
        current = Objects.requireNonNull(config, "config");
    }
}
