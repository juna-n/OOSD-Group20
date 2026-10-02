package org.oosd.audio;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.oosd.persistence.ConfigManager;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/*
plays the background music and the sound effects

Singleton (same holder idiom as ConfigManager): the app has exactly one
music track, two players or several screens must never start a second copy
created lazily on first use, which is after JavaFX has started, as media needs

observes ConfigManager, so switching music or sound with M / N or on the
configuration screen takes effect immediately
music only plays during a game and pauses with it

missing or unplayable files are reported on the console and skipped, the
game carries on silently rather than crashing
used from the JavaFX thread only
*/
public final class AudioManager implements SoundPlayer {

    static final String AUDIO_FOLDER = "/audio/";
    private static final String MUSIC_FILE = "background.mp3";

    private static final double MUSIC_VOLUME = 0.35;
    private static final double EFFECTS_VOLUME = 0.8;

    private final ConfigManager settings;
    private final Map<Sound, AudioClip> clips = new EnumMap<>(Sound.class);
    private final MediaPlayer music;   //null if the music file couldn't be loaded

    //a game screen is showing and wants music
    private boolean musicActive;
    //the game is paused (P, or a dialog is open)
    private boolean musicPaused;

    private AudioManager(ConfigManager settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
        for (Sound sound : Sound.values()) {
            loadClip(sound).ifPresent(clip -> clips.put(sound, clip));
        }
        this.music = loadMusic().orElse(null);
        settings.addListener(config -> updateMusic());
    }

    private static final class Holder {
        private static final AudioManager INSTANCE = new AudioManager(ConfigManager.getInstance());
    }

    public static AudioManager getInstance() {
        return Holder.INSTANCE;
    }

    // ---- sound effects ----

    @Override
    public void play(Sound sound) {
        if (!settings.current().soundEffects()) {
            return;
        }
        AudioClip clip = clips.get(sound);
        if (clip != null) {
            clip.play(EFFECTS_VOLUME);
        }
    }

    // ---- music ----

    //a game has started, play from the beginning (if music is switched on)
    public void playMusic() {
        musicActive = true;
        musicPaused = false;
        if (music != null) {
            music.seek(Duration.ZERO);
        }
        updateMusic();
    }

    public void setMusicPaused(boolean paused) {
        musicPaused = paused;
        updateMusic();
    }

    //game over or leaving the game screen, next playMusic() starts from the top
    public void stopMusic() {
        musicActive = false;
        musicPaused = false;
        if (music != null) {
            music.stop();
        }
    }

    public boolean isMusicPlaying() {
        return music != null && music.getStatus() == MediaPlayer.Status.PLAYING;
    }

    //single place that decides whether music should be audible right now
    private void updateMusic() {
        if (music == null || !musicActive) {
            return;
        }
        boolean shouldPlay = !musicPaused && settings.current().music();
        if (shouldPlay) {
            music.play();
        } else {
            //pause rather than stop, so switching music back on carries on where it was
            music.pause();
        }
    }

    // ---- loading ----

    private static Optional<AudioClip> loadClip(Sound sound) {
        return findResource(sound.resourcePath()).flatMap(url -> {
            try {
                return Optional.of(new AudioClip(url));
            } catch (MediaException | IllegalArgumentException e) {
                System.err.println("Could not load sound " + sound.fileName() + ": " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    private static Optional<MediaPlayer> loadMusic() {
        return findResource(AUDIO_FOLDER + MUSIC_FILE).flatMap(url -> {
            try {
                MediaPlayer player = new MediaPlayer(new Media(url));
                player.setCycleCount(MediaPlayer.INDEFINITE);
                player.setVolume(MUSIC_VOLUME);
                player.setOnError(() -> System.err.println("Music playback error: " + player.getError()));
                return Optional.of(player);
            } catch (MediaException | IllegalArgumentException e) {
                System.err.println("Could not load music " + MUSIC_FILE + ": " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    //URL string of a file on the classpath, works both from IntelliJ and from inside the jar
    private static Optional<String> findResource(String path) {
        URL url = AudioManager.class.getResource(path);
        if (url == null) {
            System.err.println("Audio file missing, expected at src/main/resources" + path);
            return Optional.empty();
        }
        return Optional.of(url.toExternalForm());
    }
}
