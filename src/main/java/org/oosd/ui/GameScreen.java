package org.oosd.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.oosd.controller.GameController;
import org.oosd.controller.GameSession;
import org.oosd.controller.command.KeyBindings;
import org.oosd.controller.player.PlayerFactory;
import org.oosd.model.GameConfig;
import org.oosd.model.HighScore;
import org.oosd.persistence.ConfigManager;
import org.oosd.persistence.HighScoreManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/*
playing screen
builds a GameSession (the controllers), shows one PlayerPanel per player,
forwards key presses to KeyBindings and drives everything from one timer
no game rules live here
*/
public class GameScreen extends BaseScreen {

    //cell size used whenever the screen is big enough to afford it
    private static final int PREFERRED_CELL = 30;
    private static final int MIN_CELL = 12;

    private static final int PADDING = 20;
    private static final int PANEL_SPACING = 40;
    //title bar, status bar, back button and padding, measured roughly
    private static final int VERTICAL_CHROME = 190;

    private final ConfigManager settings = ConfigManager.getInstance();
    private final GameConfig config;
    private final GameSession session;
    private final KeyBindings keys;
    private final List<PlayerPanel> panels = new ArrayList<>();

    //M and S change the saved config, this keeps the status bar in step with it
    private final Consumer<GameConfig> settingsListener = this::showSettings;

    private Label settingsLabel;
    private AnimationTimer timer;
    private long lastFrameNanos;
    private boolean finished;
    private boolean scoresRecorded;

    public GameScreen(Navigator navigator) {
        super(navigator);
        this.config = settings.current();
        this.session = new GameSession(config, new PlayerFactory());
        this.keys = KeyBindings.forSession(session, settings);
    }

    @Override
    public boolean sizesWindowToContent() {
        return true;
    }

    @Override
    protected Parent buildRoot() {
        int cell = cellSizeFor(config);
        int playerCount = session.controllers().size();

        HBox fields = new HBox(PANEL_SPACING);
        fields.setAlignment(Pos.CENTER);
        for (GameController controller : session.controllers()) {
            String hint = controller.player().acceptsKeyboard()
                    ? KeyBindings.controlsHint(controller.playerNumber(), playerCount)
                    : "Controlled by " + controller.playerType().label();
            PlayerPanel panel = new PlayerPanel(controller, cell, hint);
            panels.add(panel);
            fields.getChildren().add(panel.getRoot());
        }

        settingsLabel = new Label();
        settingsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #c9c9d1;");
        Label keysLabel = new Label("P  pause    M  music    S  sound    Esc  menu");
        keysLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #8a8a97;");
        HBox statusBar = new HBox(30, settingsLabel, keysLabel);
        statusBar.setAlignment(Pos.CENTER);
        showSettings(settings.current());

        Button backButton = new Button("Back to Menu");
        backButton.setPrefWidth(150);
        //without this the button grabs keyboard focus and Space would press it
        backButton.setFocusTraversable(false);
        backButton.setOnAction(event -> confirmLeave());

        VBox layout = new VBox(16, statusBar, fields, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(PADDING));
        layout.setStyle("-fx-background-color: #1b1b22;");

        //Esc is bound like any other key, written as a lambda Command
        keys.bind(KeyCode.ESCAPE, this::confirmLeave);

        //key events go to the focused node, so the root has to be focusable
        layout.setFocusTraversable(true);
        layout.setOnKeyPressed(this::handleKey);

        settings.addListener(settingsListener);
        return layout;
    }

    /*
    largest cell size, up to the preferred one, that lets every field and
    side panel fit on this monitor, so a 15x30 two player game still fits
    */
    private static int cellSizeFor(GameConfig config) {
        Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        int players = config.playerCount();

        double usableHeight = screen.getHeight() - VERTICAL_CHROME;
        double usableWidth = screen.getWidth() - 2 * PADDING
                - players * (PlayerPanel.SIDEBAR_WIDTH + PlayerPanel.GAP)
                - (players - 1) * PANEL_SPACING;

        int fitted = (int) Math.min(usableHeight / config.fieldHeight(),
                usableWidth / (players * config.fieldWidth()));
        return Math.clamp(fitted, MIN_CELL, PREFERRED_CELL);
    }

    @Override
    public void onShow() {
        getRoot().requestFocus();
        session.start();

        lastFrameNanos = 0;
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick(now);
            }
        };
        timer.start();
    }

    //runs once per frame, roughly 60fps
    private void tick(long now) {
        double elapsedSeconds = lastFrameNanos == 0 ? 0 : (now - lastFrameNanos) / 1_000_000_000.0;
        lastFrameNanos = now;

        session.update(elapsedSeconds);
        panels.forEach(PlayerPanel::render);

        if (session.isOver() && !finished) {
            finished = true;
            timer.stop();
            //dialogs can't block inside an animation pulse, so show them just after
            Platform.runLater(this::recordHighScores);
        }
    }

    private void handleKey(KeyEvent event) {
        if (keys.handle(event.getCode())) {
            panels.forEach(PlayerPanel::render);
            event.consume();
        }
    }

    private void showSettings(GameConfig current) {
        settingsLabel.setText("Music: " + onOff(current.music())
                + "     Sound: " + onOff(current.soundEffects()));
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    // ---- leaving and scores ----

    private void confirmLeave() {
        if (finished) {
            leave();
            return;
        }

        boolean pausedByPlayer = session.isPaused();
        session.setPaused(true);

        boolean confirmed = Dialogs.confirm(getRoot(), "Back to Menu",
                "Stop the current game and return to the menu?\n"
                        + "Any score that makes the top 10 will still be recorded.");

        if (confirmed) {
            finished = true;
            timer.stop();
            session.endAll();
            recordHighScores();
            leave();
            return;
        }

        session.setPaused(pausedByPlayer);
        //the dialog took focus, take it back or the keys stay dead
        getRoot().requestFocus();
    }

    //asks each player whose score made the top 10 for a name, one after another
    private void recordHighScores() {
        if (scoresRecorded) {
            return;
        }
        scoresRecorded = true;

        HighScoreManager scores = HighScoreManager.getInstance();
        for (GameController controller : session.controllers()) {
            int score = controller.state().score();
            if (!scores.qualifies(score)) {
                continue;
            }
            String who = "Player " + controller.playerNumber() + " (" + controller.playerType().label() + ")";
            Dialogs.askName(getRoot(), "New High Score",
                            who + " scored " + score + " and made the top 10!")
                    .ifPresent(name -> scores.submit(new HighScore(
                            name, score, config.summary(controller.playerNumber()))));
        }
    }

    private void leave() {
        if (timer != null) {
            timer.stop();
        }
        session.shutdown();
        settings.removeListener(settingsListener);
        panels.forEach(PlayerPanel::detach);
        navigator.show(new MainMenuScreen(navigator));
    }
}