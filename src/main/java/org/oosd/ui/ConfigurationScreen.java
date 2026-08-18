package org.oosd.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.oosd.model.ConfigStore;
import org.oosd.model.GameConfig;

//configuration screen, all the controls write straight to configstore
public class ConfigurationScreen extends BaseScreen {

    private Slider widthSlider;
    private Slider heightSlider;
    private Slider levelSlider;
    private CheckBox musicBox;
    private CheckBox soundEffectsBox;
    private CheckBox aiPlayBox;
    private CheckBox extendedModeBox;

    private boolean ready;

    public ConfigurationScreen(Navigator navigator) {
        super(navigator);
    }

    @Override
    protected Parent buildRoot() {
        GameConfig config = ConfigStore.current();

        Label heading = new Label("Configuration");
        heading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f2f2f2;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setAlignment(Pos.CENTER);

        widthSlider = addSlider(grid, 0, "Field Width (cols)",
                GameConfig.MIN_WIDTH, GameConfig.MAX_WIDTH, config.fieldWidth());
        heightSlider = addSlider(grid, 1, "Field Height (rows)",
                GameConfig.MIN_HEIGHT, GameConfig.MAX_HEIGHT, config.fieldHeight());
        levelSlider = addSlider(grid, 2, "Game Level",
                GameConfig.MIN_LEVEL, GameConfig.MAX_LEVEL, config.startLevel());

        musicBox = addCheckBox(grid, 3, "Music", config.music());
        soundEffectsBox = addCheckBox(grid, 4, "Sound Effects", config.soundEffects());
        aiPlayBox = addCheckBox(grid, 5, "AI Play", config.aiPlay());
        extendedModeBox = addCheckBox(grid, 6, "Extended Mode", config.extendedMode());

        Button backButton = new Button("Back");
        backButton.setPrefWidth(200);
        backButton.setPrefHeight(40);
        backButton.setOnAction(event -> navigator.show(new MainMenuScreen(navigator)));

        VBox layout = new VBox(28, heading, grid, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #1b1b22;");

        ready = true;
        return layout;
    }

    private Slider addSlider(GridPane grid, int row, String caption, int min, int max, int initial) {
        Slider slider = new Slider(min, max, initial);
        slider.setPrefWidth(260);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setShowTickMarks(true);

        Label valueLabel = new Label(String.valueOf(initial));
        valueLabel.setMinWidth(32);
        valueLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #f2f2f2;");

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            valueLabel.setText(String.valueOf(newValue.intValue()));
            save();
        });

        grid.add(caption(caption), 0, row);
        grid.add(slider, 1, row);
        grid.add(valueLabel, 2, row);
        return slider;
    }

    private CheckBox addCheckBox(GridPane grid, int row, String caption, boolean initial) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(initial);

        Label stateLabel = new Label(initial ? "On" : "Off");
        stateLabel.setMinWidth(32);
        stateLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #f2f2f2;");

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            stateLabel.setText(newValue ? "On" : "Off");
            save();
        });

        grid.add(caption(caption), 0, row);
        grid.add(checkBox, 1, row);
        grid.add(stateLabel, 2, row);
        return checkBox;
    }

    private Label caption(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #c9c9d1;");
        return label;
    }

    private void save() {
        if (!ready) {
            return;
        }
        ConfigStore.update(new GameConfig(
                (int) widthSlider.getValue(),
                (int) heightSlider.getValue(),
                (int) levelSlider.getValue(),
                musicBox.isSelected(),
                soundEffectsBox.isSelected(),
                aiPlayBox.isSelected(),
                extendedModeBox.isSelected()));
    }
}
