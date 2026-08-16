package org.oosd.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

//splash screen with group members

public class SplashScreen extends BaseScreen {

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

        VBox layout = new VBox(14, title, course, group, members);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #1b1b22;");
        return layout;
    }
}
