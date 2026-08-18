package org.oosd.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//dummy data for now
public final class HighScoreStore {

    private static final int MAX_ENTRIES = 10;

    private static final List<HighScore> scores = new ArrayList<>(List.of(
            new HighScore("Bayden", 24800),
            new HighScore("Juna", 19350),
            new HighScore("Pedro", 17420),
            new HighScore("Ollie", 15100),
            new HighScore("Ted", 13675),
            new HighScore("Oscar", 11200),
            new HighScore("Ella", 9840),
            new HighScore("Alexa", 7615),
            new HighScore("Holly", 5230),
            new HighScore("Sam", 3105)));

    private HighScoreStore() {
        //utility class
    }

    //the stored scores, highest first, max 10 entries
    public static List<HighScore> topTen() {
        return scores.stream().sorted(Comparator.comparingInt(HighScore::score).reversed()).limit(MAX_ENTRIES).toList();
    }

    //records new score and keeps table at 10 entries, not used until scoring is later implemented
    public static void submit(HighScore entry) {
        scores.add(entry);
        List<HighScore> trimmed = topTen();
        scores.clear();
        scores.addAll(trimmed);
    }
}
