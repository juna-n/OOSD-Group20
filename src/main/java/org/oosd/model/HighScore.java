package org.oosd.model;

import java.util.Comparator;

/*
one row of the high score table
natural ordering (Comparable) is highest score first, so a list of scores
can simply be sorted() to get the table order
*/
public record HighScore(String playerName, int score, String configSummary)
        implements Comparable<HighScore> {

    public static final int MAX_NAME_LENGTH = 16;

    public static final Comparator<HighScore> HIGHEST_FIRST =
            Comparator.comparingInt(HighScore::score).reversed();

    public HighScore {
        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative: " + score);
        }
        playerName = cleanName(playerName);
        //scores saved before the config column existed have no summary
        configSummary = configSummary == null || configSummary.isBlank() ? "----" : configSummary.strip();
    }

    //scores with equal points compare as equal, sorting is stable so the earlier one stays above
    @Override
    public int compareTo(HighScore other) {
        return HIGHEST_FIRST.compare(this, other);
    }

    private static String cleanName(String name) {
        if (name == null || name.isBlank()) {
            return "Anonymous";
        }
        String stripped = name.strip();
        return stripped.length() > MAX_NAME_LENGTH ? stripped.substring(0, MAX_NAME_LENGTH) : stripped;
    }
}