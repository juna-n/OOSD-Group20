package org.oosd.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

//main screen, Play, Configuration, High Scores, Exit
public class MainMenuScreen extends BaseScreen {

    //main menu background image to be included later
    //private static final String BACKGROUND_PATH = "/images/menu-background.png";

    public MainMenuScreen(Navigator navigator) {
        super(navigator);
    }

    @Override
    protected Parent buildRoot() {
        Label heading = styledLabel("Main Menu", "heading");

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
        //remove this style class when including the image, otherwise the colour paints over it
        layout.getStyleClass().add("screen");

        //slightly stronger scrim than the splash screen, helps keep buttons readable
        //return withBackground(BACKGROUND_PATH, layout, 0.62);

        //also remove when adding background image
        return layout;
    }

    private Button menuButton(String text) {
        Button button = new Button(text);
        //size and look come from .menu-button in tetris.css
        button.getStyleClass().add("menu-button");
        return button;
    }

    //shares Dialogs.confirm with the game screen so both prompts match
    private void confirmExit() {
        if (Dialogs.confirm(getRoot(), "Exit", "Are you sure you want to exit?")) {
            navigator.exitApp();
        }
    }
}