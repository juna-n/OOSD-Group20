package org.oosd.persistence;

import org.oosd.model.GameConfig;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/*
the one place the current configuration lives, saved to JSON on every change
Singleton using the initialization-on-demand holder idiom:
- lazy: Holder is only loaded (and the file only read) the first time
  getInstance() is called
- thread safe: the JVM guarantees a class is initialised exactly once,
  even if several threads call getInstance() at the same moment, so no
  synchronized or volatile is needed on the lookup itself
*/
public final class ConfigManager {

    public static final Path DEFAULT_FILE = Path.of("tetris-config.json");

    private final JsonStore<GameConfig> store;
    private final List<Consumer<GameConfig>> listeners = new CopyOnWriteArrayList<>();

    //volatile so a value written by one thread is seen straight away by others
    private volatile GameConfig current;

    private ConfigManager() {
        this(JsonStore.of(DEFAULT_FILE, GameConfig.class));
    }

    //package-private so tests in the same package can point it at a temp file
    ConfigManager(JsonStore<GameConfig> store) {
        this.store = Objects.requireNonNull(store, "store");

        GameConfig loaded = store.load().orElse(null);
        if (loaded == null) {
            //first run or unreadable file: start from defaults and write them out
            loaded = GameConfig.DEFAULT;
            store.save(loaded);
        }
        this.current = loaded;
    }

    private static final class Holder {
        private static final ConfigManager INSTANCE = new ConfigManager();
    }

    public static ConfigManager getInstance() {
        return Holder.INSTANCE;
    }

    public GameConfig current() {
        return current;
    }

    //replaces the config, writes it to disk and tells every listener
    public synchronized void update(GameConfig config) {
        Objects.requireNonNull(config, "config");
        if (config.equals(current)) {
            return;
        }
        current = config;
        store.save(config);
        listeners.forEach(listener -> listener.accept(config));
    }

    //lets the game screen react immediately when M or S changes music or sound
    public void addListener(Consumer<GameConfig> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeListener(Consumer<GameConfig> listener) {
        listeners.remove(listener);
    }
}
