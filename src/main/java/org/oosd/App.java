package org.oosd;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.oosd.ui.Navigator;
import org.oosd.ui.Screen;
import org.oosd.ui.SplashScreen;

/*
 application entry point
 one Stage exists for the whole run, each screen gets a fresh Scene so the
 window can be resized to suit it: the game screen sizes the window to its
 content (field size, one or two players), every other screen puts the
 window back to the standard size, and the window is re-centred each time
*/
public class App extends Application implements Navigator {

    public static final int WINDOW_WIDTH = 640;
    public static final int WINDOW_HEIGHT = 760;

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("OOSD-Group20 Tetris");
        stage.setResizable(true);
        show(new SplashScreen(this));
    }

    @Override
    public void show(Screen screen) {
        Scene scene = screen.sizesWindowToContent()
                ? new Scene(screen.getRoot())
                : new Scene(screen.getRoot(), WINDOW_WIDTH, WINDOW_HEIGHT);

        //a maximised window ignores sizeToScene, so un-maximise first
        stage.setMaximized(false);
        stage.setScene(scene);
        stage.sizeToScene();
        if (!stage.isShowing()) {
            stage.show();
        }
        stage.centerOnScreen();

        screen.onShow();
    }

    @Override
    public void exitApp() {
        stage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}