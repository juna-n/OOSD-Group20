package org.oosd.ui;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;

import java.net.URL;

//finds the shared stylesheet once and attaches it to scenes and dialogs
public final class Styles {

    private static final String STYLESHEET_PATH = "/styles/tetris.css";

    //null if the file is missing, the program still runs, just unstyled
    private static final String STYLESHEET = locate();

    private Styles() {
        //utility class
    }

    public static void applyTo(Scene scene) {
        if (STYLESHEET != null) {
            scene.getStylesheets().add(STYLESHEET);
        }
    }

    //dialogs open in their own window, so they need the stylesheet separately
    public static void applyTo(DialogPane pane) {
        if (STYLESHEET != null) {
            pane.getStylesheets().add(STYLESHEET);
        }
    }

    private static String locate() {
        URL url = Styles.class.getResource(STYLESHEET_PATH);
        if (url == null) {
            System.err.println("Stylesheet missing, expected at src/main/resources" + STYLESHEET_PATH);
            return null;
        }
        return url.toExternalForm();
    }
}
