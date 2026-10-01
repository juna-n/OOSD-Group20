package org.oosd.controller.command;

/*
how a held key repeats: act once on press, wait delaySeconds, then act again
every intervalSeconds until released
these replace the operating system's own key repeat, which waits about
half a second before starting and only ever repeats the last key pressed
*/
public record RepeatRate(double delaySeconds, double intervalSeconds) {

    //left and right, lower delay = quicker slide when held, lower interval = faster slide
    public static final RepeatRate SHIFT = new RepeatRate(0.17, 0.05);

    //down, starts almost immediately so holding it feels like a fast fall
    public static final RepeatRate SOFT_DROP = new RepeatRate(0.05, 0.05);

    public RepeatRate {
        if (delaySeconds < 0) {
            throw new IllegalArgumentException("Delay cannot be negative: " + delaySeconds);
        }
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("Interval must be positive: " + intervalSeconds);
        }
    }
}
