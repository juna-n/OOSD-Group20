package org.oosd.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

//main screen, Play, Configuration, High Scores, Exit
public class MainMenuScreen extends BaseScreen {

    public MainMenuScreen(Navigator navigator) {
        super(navigator);
    }

    @Override
    protected Parent buildRoot() {
        Label heading = new Label("Main Menu");
        heading.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f2f2f2;");

        Button play = menuButton("Play");
        Button configuration = menuButton("Configuration");
        Button highScores = menuButton("High Scores");
        Button exit = menuButton("Exit");

        play.setOnAction(event -> navigator.show(new GameScreen(navigator)));
        configuration.setOnAction(event -> navigator.show(new ConfigurationScreen(navigator)));
        highScores.setOnAction(event -> navigator.show(new HighScoreScreen(navigator)));
        exit.setOnAction(event -> confirmExit());

        VBox layout = new VBox(14, heading, play, configuration, highScores, exit);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #1b1b22;");
        return layout;
    }

    private Button menuButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(220);
        button.setPrefHeight(42);
        return button;
    }

    private void confirmExit() {
        if (Dialogs.confirm(getRoot(), "Exit", "Are you sure you want to exit?")) {
            navigator.exitApp();
        }
    }
}
