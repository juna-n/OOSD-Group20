package org.oosd.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.oosd.controller.GameController;
import org.oosd.model.Cell;
import org.oosd.model.GameBoard;
import org.oosd.model.GameListener;
import org.oosd.model.GameState;
import org.oosd.model.PlayerType;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoType;

/*
the view for one player: the playing field plus a side panel of stats
the field canvas is redrawn every frame, the stat labels only change when
the model says something happened (it is a GameListener)
*/
public final class PlayerPanel {

    public static final int SIDEBAR_WIDTH = 170;
    public static final int GAP = 16;

    private static final int PREVIEW_CELL = 18;
    private static final int PREVIEW_BOX = 4;

    private static final Color FIELD_BACKGROUND = Color.web("#101016");
    private static final Color GRID_LINE = Color.web("#26262f");
    private static final Color WARNING = Color.web("#d93b3b");

    private final GameController controller;
    private final GameState state;
    private final int cell;

    private final Canvas fieldCanvas;
    private final Canvas previewCanvas;
    private final Label levelValue;
    private final Label linesValue;
    private final Label scoreValue;
    private final Label statusLabel;
    private final HBox root;

    //observer: refresh the numbers whenever the game reports an event
    private final GameListener statsListener = (event, source) -> refreshStats();

    public PlayerPanel(GameController controller, int cellSize, String controlsHint) {
        this.controller = controller;
        this.state = controller.state();
        this.cell = cellSize;

        GameBoard board = state.board();
        fieldCanvas = new Canvas(board.cols() * cell, board.rows() * cell);
        previewCanvas = new Canvas(PREVIEW_BOX * PREVIEW_CELL, PREVIEW_BOX * PREVIEW_CELL);

        Label title = styled(new Label("Player " + controller.playerNumber()), "player-title");

        levelValue = statValue("");
        linesValue = statValue("");
        scoreValue = statValue("");

        statusLabel = styled(new Label(), "warning-text");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(SIDEBAR_WIDTH);

        Label hint = styled(new Label(controlsHint), "hint");

        VBox sidebar = new VBox(6,
                title,
                statCaption("Player Type"), statValue(controller.playerType().label()),
                statCaption("Initial Level"), statValue(String.valueOf(state.startLevel())),
                statCaption("Current Level"), levelValue,
                statCaption("Lines Erased"), linesValue,
                statCaption("Score"), scoreValue,
                statCaption("Next"), previewCanvas,
                statusLabel,
                hint);
        sidebar.setAlignment(Pos.TOP_LEFT);
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.setMinWidth(SIDEBAR_WIDTH);

        root = new HBox(GAP, fieldCanvas, sidebar);
        root.setAlignment(Pos.TOP_CENTER);

        state.addListener(statsListener);
        refreshStats();
    }

    public Node getRoot() {
        return root;
    }

    //stop listening, called when the game screen is left so this panel can be garbage collected
    public void detach() {
        state.removeListener(statsListener);
    }

    private void refreshStats() {
        levelValue.setText(String.valueOf(state.level()));
        linesValue.setText(String.valueOf(state.rowsCleared()));
        scoreValue.setText(String.valueOf(state.score()));
    }

    // ---- drawing, called every frame ----

    public void render() {
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

        if (!state.isGameOver()) {
            double offset = state.canFall() ? controller.fallProgress() : 0.0;
            Tetromino piece = state.current();
            Color pieceColor = BlockPalette.colorOf(piece.type());
            for (Cell blockCell : piece.cells()) {
                if (blockCell.row() >= 0) {
                    drawBlock(gc, blockCell.col() * cell, (blockCell.row() + offset) * cell, cell, pieceColor);
                }
            }
        }

        //an automated player that can't decide (e.g. server not running) has no control
        String status = controller.player().status();
        if (!status.isEmpty() && !state.isGameOver()) {
            drawWarningBanner(gc, controller.playerType() == PlayerType.EXTERNAL ? "SERVER OFFLINE" : "NO CONTROL");
        }

        if (state.isGameOver()) {
            drawOverlay(gc, "GAME OVER", "Final score: " + state.score());
        } else if (state.isPaused()) {
            drawOverlay(gc, "PAUSED", "Press P to resume");
        }

        statusLabel.setText(status);
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

    //red strip across the top of the field, the details are in the side panel
    private void drawWarningBanner(GraphicsContext gc, String text) {
        double height = Math.max(22, cell);
        gc.setFill(WARNING.deriveColor(0, 1, 1, 0.9));
        gc.fillRect(0, 0, fieldCanvas.getWidth(), height);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, Math.min(16, fieldCanvas.getWidth() / 12)));
        gc.fillText(text, fieldCanvas.getWidth() / 2, height / 2 + 5);
    }

    private void drawOverlay(GraphicsContext gc, String title, String subtitle) {
        gc.setFill(Color.color(0, 0, 0, 0.72));
        gc.fillRect(0, 0, fieldCanvas.getWidth(), fieldCanvas.getHeight());

        double centreX = fieldCanvas.getWidth() / 2;
        double centreY = fieldCanvas.getHeight() / 2;

        //smaller fields get smaller text so the title still fits
        double titleSize = Math.min(32, fieldCanvas.getWidth() / 6);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, titleSize));
        gc.fillText(title, centreX, centreY - 8);
        gc.setFont(Font.font("System", 14));
        gc.setFill(Color.web("#c9c9d1"));
        gc.fillText(subtitle, centreX, centreY + 22);
    }

    private static Label statCaption(String text) {
        return styled(new Label(text.toUpperCase()), "stat-caption");
    }

    private static Label statValue(String text) {
        return styled(new Label(text), "stat-value");
    }

    //text styles live in tetris.css, the canvas colours above stay in code because a Canvas can't be styled with CSS
    private static Label styled(Label label, String styleClass) {
        label.getStyleClass().add(styleClass);
        return label;
    }
}