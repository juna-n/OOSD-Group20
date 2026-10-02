package org.oosd.ui;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import java.net.URL;

public abstract class BaseScreen implements Screen {

    protected final Navigator navigator;
    private Parent root;

    protected BaseScreen(Navigator navigator) {
        this.navigator = navigator;
    }

    //builds this screens node tree, called at most once per instance
    protected abstract Parent buildRoot();

    @Override
    public final Parent getRoot() {
        if (root == null) {
            root = buildRoot();
        }
        return root;
    }

    //a label with one of the text styles from tetris.css, e.g. "heading" or "caption"
    protected static Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private static final double DEFAULT_SCRIM_OPACITY = 0.55;

    //wraps content over a background image, using the default scrim strength
    protected Parent withBackground(String resourcePath, Node content) {
        return withBackground(resourcePath, content, DEFAULT_SCRIM_OPACITY);
    }

    protected Parent withBackground(String resourcePath, Node content, double scrimOpacity) {
        StackPane layered = new StackPane();
        //screen background colour comes from tetris.css, also the fallback when an image is missing
        layered.getStyleClass().add("screen");

        URL image = getClass().getResource(resourcePath);
        if (image != null) {
            layered.getChildren().addAll(backgroundLayer(image), scrimLayer(scrimOpacity));
        } else {
            //prevent a missing asset stopping the program from starting
            System.err.println("Background image not found on classpath: " + resourcePath);
        }

        layered.getChildren().add(content);
        return layered;
    }

    /*
    a Region with a CSS background rather than an ImageView, because
    -fx-background-size: cover fills the window at any size without
    distorting the image, inline because the image URL is only known at runtime
    */
    private static Region backgroundLayer(URL image) {
        Region background = new Region();
        background.setStyle(
                "-fx-background-image: url('" + image.toExternalForm() + "');"
                        + "-fx-background-size: cover;"
                        + "-fx-background-position: center center;"
                        + "-fx-background-repeat: no-repeat;");
        return background;
    }

    private static Region scrimLayer(double opacity) {
        Region scrim = new Region();
        scrim.setStyle("-fx-background-color: rgba(16, 16, 22, " + opacity + ");");
        return scrim;
    }
}