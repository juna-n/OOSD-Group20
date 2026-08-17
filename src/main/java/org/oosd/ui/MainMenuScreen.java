package org.oosd.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

//main menu, with Play, Configuration, High Scores, and Exit

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

        //TODO: navigator.show(new GameScreen(navigator));
        play.setOnAction(event -> System.out.println("Play not implemented yet"));
        //TODO: navigator.show(new ConfigurationScreen(navigator));
        configuration.setOnAction(event -> System.out.println("Configuration not implemented yet"));
        //TODO: navigator.show(new HighScoreScreen(navigator));
        highScores.setOnAction(event -> System.out.println("High scores not implemented yet"));
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

    //exit confirmation pop up
    private void confirmExit() {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to exit?",
                ButtonType.YES,
                ButtonType.NO);
        alert.setTitle("Exit");
        alert.setHeaderText(null);
        alert.showAndWait()
                .filter(response -> response == ButtonType.YES)
                .ifPresent(response -> navigator.exitApp());
    }
}
