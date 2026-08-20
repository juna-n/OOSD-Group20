package org.oosd.ui;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

//splash screen with group members
public class SplashScreen extends BaseScreen {

    private static final Duration SPLASH_DURATION = Duration.seconds(3);
    private static final String BACKGROUND_PATH = "/images/splash-background.png";

    public SplashScreen(Navigator navigator) {
        super(navigator);
    }

    @Override
    protected Parent buildRoot() {
        Label title = new Label("TETRIS");
        title.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: #f2f2f2;");

        Label course = new Label("2006ICT Object Oriented Software Development");
        course.setStyle("-fx-font-size: 16px; -fx-text-fill: #cfcfcf;");

        Label group = new Label("Group 20 \u2014 Gold Coast Campus");
        group.setStyle("-fx-font-size: 16px; -fx-text-fill: #cfcfcf;");

        Label members = new Label("Oscar Unicomb-Dodds \u00b7 Juna Nakanishi \u00b7 Pedro Penna Navarrete");
        members.setStyle("-fx-font-size: 14px; -fx-text-fill: #9f9f9f;");

        VBox textLayer = new VBox(14, title, course, group, members);
        textLayer.setAlignment(Pos.CENTER);

        //layering is inherited from BaseScreen so the menu can reuse it
        return withBackground(BACKGROUND_PATH, textLayer);
    }

    @Override
    public void onShow() {
        PauseTransition delay = new PauseTransition(SPLASH_DURATION);
        delay.setOnFinished(event -> navigator.show(new MainMenuScreen(navigator)));
        delay.play();
    }
}
