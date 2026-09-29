package org.oosd.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.oosd.model.Cell;
import org.oosd.model.GameBoard;
import org.oosd.model.GameConfig;
import org.oosd.model.GameState;
import org.oosd.model.HighScore;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoType;
import org.oosd.persistence.ConfigManager;
import org.oosd.persistence.HighScoreManager;

//playing screen, game logic lives in GameState
public class GameScreen extends BaseScreen {

    //cell size used whenever the field is small enough to afford it
    private static final int PREFERRED_CELL = 30;
    private static final int MIN_CELL = 12;

    private static final int SIDEBAR_WIDTH = 170;
    private static final int LAYOUT_PADDING = 24;
    private static final int LAYOUT_GAP = 24;
    private static final int FIELD_MAX_WIDTH_PX = 640 - (2 * LAYOUT_PADDING) - LAYOUT_GAP - SIDEBAR_WIDTH;
    private static final int FIELD_MAX_HEIGHT_PX = 760 - (2 * LAYOUT_PADDING) - 12;

    private static final int PREVIEW_CELL = 18;
    private static final int PREVIEW_BOX = 4;

    private static final Color FIELD_BACKGROUND = Color.web("#101016");
    private static final Color GRID_LINE = Color.web("#26262f");

    private final GameConfig config;
    private final GameState state;

    //pixel size of one cell for this game, not a constant because the config
    //screen lets the field grow to 15x30, which at 30px would need a 450x900
    //field and overflows the window in both directions
    private final int cell;

    private Canvas fieldCanvas;
    private Canvas previewCanvas;
    private Label scoreValue;
    private Label levelValue;
    private Label rowsValue;

    private AnimationTimer timer;

    private double fallProgress;

    private long lastFrameNanos;

    //the name prompt must only ever appear once per game
    private boolean highScoreOffered;

    public GameScreen(Navigator navigator) {
        super(navigator);
        this.config = ConfigManager.getInstance().current();
        this.state = new GameState(config);
        this.cell = fittedCellSize(state.board().cols(), state.board().rows());
    }

    //largest cell size, up to the preferred one, that keeps the field on screen
    private static int fittedCellSize(int cols, int rows) {
        int limitedByWidth = FIELD_MAX_WIDTH_PX / cols;
        int limitedByHeight = FIELD_MAX_HEIGHT_PX / rows;
        int fitted = Math.min(limitedByWidth, limitedByHeight);
        return Math.max(MIN_CELL, Math.min(PREFERRED_CELL, fitted));
    }

    @Override
    protected Parent buildRoot() {
        GameBoard board = state.board();
        fieldCanvas = new Canvas(board.cols() * cell, board.rows() * cell);
        previewCanvas = new Canvas(PREVIEW_BOX * PREVIEW_CELL, PREVIEW_BOX * PREVIEW_CELL);

        scoreValue = statValue("0");
        levelValue = statValue(String.valueOf(state.level()));
        rowsValue = statValue("0");

        Button backButton = new Button("Back to Menu");
        backButton.setPrefWidth(150);
        //without this the button grabs keyboard focus and the arrow keys start
        //navigating the UI instead of moving the tetromino
        backButton.setFocusTraversable(false);
        backButton.setOnAction(event -> confirmLeave());

        VBox sidebar = new VBox(8,
                statCaption("Next"), previewCanvas,
                statCaption("Score"), scoreValue,
                statCaption("Level"), levelValue,
                statCaption("Rows"), rowsValue,
                new Label(" "),
                controlsHint(),
                backButton);
        sidebar.setAlignment(Pos.TOP_LEFT);
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.setMinWidth(SIDEBAR_WIDTH);

        HBox layout = new HBox(LAYOUT_GAP, fieldCanvas, sidebar);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(LAYOUT_PADDING));
        layout.setStyle("-fx-background-color: #1b1b22;");

        //key events go to the focused node, so the root has to be focusable
        //and has to actually hold focus
        layout.setFocusTraversable(true);
        layout.setOnKeyPressed(this::handleKey);

        return layout;
    }

    @Override
    public void onShow() {
        getRoot().requestFocus();
        lastFrameNanos = 0;
        fallProgress = 0;

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
        if (lastFrameNanos == 0) {
            lastFrameNanos = now;
            render();
            return;
        }

        double elapsedSeconds = (now - lastFrameNanos) / 1_000_000_000.0;
        lastFrameNanos = now;

        if (!state.isPaused() && !state.isGameOver()) {
            fallProgress += elapsedSeconds / state.secondsPerRow();

            while (fallProgress >= 1.0) {
                fallProgress -= 1.0;
                state.step();
            }
        }

        render();

        if (state.isGameOver()) {
            timer.stop();
            //dialogs can't block inside an animation pulse, so show it just after
            Platform.runLater(this::offerHighScore);
        }
    }

    //asks for a name if the score made the top 10, then saves it to the JSON file
    private void offerHighScore() {
        HighScoreManager scores = HighScoreManager.getInstance();
        if (highScoreOffered || !scores.qualifies(state.score())) {
            return;
        }
        highScoreOffered = true;

        Dialogs.askName(getRoot(), "New High Score",
                        "Your score of " + state.score() + " made the top 10!")
                .ifPresent(name -> scores.submit(
                        new HighScore(name, state.score(), config.summary(1))));
    }

    private void handleKey(KeyEvent event) {
        switch (event.getCode()) {
            case LEFT -> state.moveLeft();
            case RIGHT -> state.moveRight();
            case UP -> state.rotate();
            case DOWN -> {
                state.softDrop();
                fallProgress = 0;
            }
            case SPACE -> {
                state.hardDrop();
                fallProgress = 0;
            }
            case P -> state.togglePause();
            case ESCAPE -> confirmLeave();
            default -> {
                //every other key is ignored
            }
        }
        render();
        event.consume();
    }

    private void confirmLeave() {
        if (state.isGameOver()) {
            leave();
            return;
        }

        boolean pausedByPlayer = state.isPaused();

        if (!pausedByPlayer) {
            state.togglePause();
            render();
        }

        boolean confirmed = Dialogs.confirm(getRoot(), "Back to Menu",
                "Return to the main menu? Your current game will be lost.");

        if (confirmed) {
            leave();
            return;
        }

        if (!pausedByPlayer) {
            state.togglePause();
        }
        //the dialog took focus, take it back or the arrow keys stay dead
        getRoot().requestFocus();
        render();
    }

    private void leave() {
        if (timer != null) {
            timer.stop();
        }
        navigator.show(new MainMenuScreen(navigator));
    }

    private void render() {
        GraphicsContext gc = fieldCanvas.getGraphicsContext2D();
        GameBoard board = state.board();

        gc.setFill(FIELD_BACKGROUND);
        gc.fillRect(0, 0, fieldCanvas.getWidth(), fieldCanvas.getHeight());
        drawGridLines(gc, board);

        //blocks already locked into the field, each remember the type it came
        //from, so colours persist after the piece itself is gone
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                TetrominoType type = board.blockAt(row, col);
                if (type != null) {
                    drawBlock(gc, col * cell, row * cell, cell, BlockPalette.colorOf(type));
                }
            }
        }

        double offset = state.canFall() ? fallProgress : 0.0;
        Tetromino piece = state.current();
        Color pieceColor = BlockPalette.colorOf(piece.type());
        for (Cell blockCell : piece.cells()) {
            if (blockCell.row() >= 0) {
                drawBlock(gc, blockCell.col() * cell, (blockCell.row() + offset) * cell, cell, pieceColor);
            }
        }

        if (state.isPaused()) {
            drawOverlay(gc, "PAUSED", "Press P to resume");
        } else if (state.isGameOver()) {
            drawOverlay(gc, "GAME OVER", "Final score: " + state.score());
        }

        scoreValue.setText(String.valueOf(state.score()));
        levelValue.setText(String.valueOf(state.level()));
        rowsValue.setText(String.valueOf(state.rowsCleared()));
        renderPreview();
    }

    private void renderPreview() {
        GraphicsContext gc = previewCanvas.getGraphicsContext2D();
        gc.setFill(FIELD_BACKGROUND);
        gc.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());

        TetrominoType type = state.next();
        Color color = BlockPalette.colorOf(type);
        double inset = (PREVIEW_BOX - type.boxSize()) * PREVIEW_CELL / 2.0;
        for (Cell blockCell : type.baseCells()) {
            drawBlock(gc,
                    inset + blockCell.col() * PREVIEW_CELL,
                    inset + blockCell.row() * PREVIEW_CELL,
                    PREVIEW_CELL, color);
        }
    }

    private void drawGridLines(GraphicsContext gc, GameBoard board) {
        gc.setStroke(GRID_LINE);
        gc.setLineWidth(1);
        for (int col = 1; col < board.cols(); col++) {
            gc.strokeLine(col * cell, 0, col * cell, fieldCanvas.getHeight());
        }
        for (int row = 1; row < board.rows(); row++) {
            gc.strokeLine(0, row * cell, fieldCanvas.getWidth(), row * cell);
        }
    }

    private void drawBlock(GraphicsContext gc, double x, double y, double size, Color color) {
        gc.setFill(color);
        gc.fillRoundRect(x + 1, y + 1, size - 2, size - 2, 5, 5);
        gc.setStroke(color.darker());
        gc.setLineWidth(2);
        gc.strokeRoundRect(x + 1, y + 1, size - 2, size - 2, 5, 5);
    }

    private void drawOverlay(GraphicsContext gc, String title, String subtitle) {
        gc.setFill(Color.color(0, 0, 0, 0.72));
        gc.fillRect(0, 0, fieldCanvas.getWidth(), fieldCanvas.getHeight());

        double centreX = fieldCanvas.getWidth() / 2;
        double centreY = fieldCanvas.getHeight() / 2;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 32));
        gc.fillText(title, centreX, centreY - 8);
        gc.setFont(Font.font("System", 15));
        gc.setFill(Color.web("#c9c9d1"));
        gc.fillText(subtitle, centreX, centreY + 22);
    }

    private Label statCaption(String text) {
        Label label = new Label(text.toUpperCase());
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a97; -fx-font-weight: bold;");
        return label;
    }

    private Label statValue(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 22px; -fx-text-fill: #f2f2f2;");
        return label;
    }

    private Label controlsHint() {
        Label label = new Label("""
                \u2190 \u2192  move
                \u2191  rotate
                \u2193  soft drop
                Space  hard drop
                P  pause""");
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a8a97;");
        return label;
    }
}