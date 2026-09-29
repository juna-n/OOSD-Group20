package org.oosd.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import org.oosd.model.HighScore;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 the top 10 table, kept sorted and saved to JSON after every change
 same holder-idiom Singleton as ConfigManager, see there for why it is
 lazy and thread safe
*/
public final class HighScoreManager {

    public static final int MAX_ENTRIES = 10;
    public static final Path DEFAULT_FILE = Path.of("tetris-scores.json");

    //anonymous subclass captures List<HighScore> at runtime, which List.class alone cannot
    private static final TypeReference<List<HighScore>> LIST_TYPE = new TypeReference<>() {
    };

    private final JsonStore<List<HighScore>> store;
    private final List<HighScore> scores = new ArrayList<>();

    private HighScoreManager() {
        this(JsonStore.of(DEFAULT_FILE, LIST_TYPE));
    }

    //package-private so tests in the same package can point it at a temp file
    HighScoreManager(JsonStore<List<HighScore>> store) {
        this.store = Objects.requireNonNull(store, "store");
        scores.addAll(store.loadOrElse(List::of));
        normalise();
    }

    private static final class Holder {
        private static final HighScoreManager INSTANCE = new HighScoreManager();
    }

    public static HighScoreManager getInstance() {
        return Holder.INSTANCE;
    }

    //highest first, at most 10, read-only copy so callers can't bypass saving
    public synchronized List<HighScore> topTen() {
        return List.copyOf(scores);
    }

    //true if this score would earn a place in the table
    public synchronized boolean qualifies(int score) {
        if (score <= 0) {
            return false;
        }
        return scores.size() < MAX_ENTRIES || score > scores.getLast().score();
    }

    //adds the entry, saves, and returns its 1-based rank, or -1 if it didn't make the table
    public synchronized int submit(HighScore entry) {
        Objects.requireNonNull(entry, "entry");
        scores.add(entry);
        normalise();
        store.save(List.copyOf(scores));
        int index = scores.indexOf(entry);
        return index < 0 ? -1 : index + 1;
    }

    public synchronized void clear() {
        scores.clear();
        store.save(List.of());
    }

    //sort highest first, drop anything past 10th, and ignore null rows from a bad file
    private void normalise() {
        List<HighScore> kept = scores.stream()
                .filter(Objects::nonNull)
                .sorted()
                .limit(MAX_ENTRIES)
                .toList();
        scores.clear();
        scores.addAll(kept);
    }
}
