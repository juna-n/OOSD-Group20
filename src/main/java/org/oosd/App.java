package org.oosd;

import org.oosd.ui.Navigator;
import org.oosd.ui.Screen;
import org.oosd.ui.SplashScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 application entry point
 one Stage and one scene exist for the whole run, changing screens swaps
 the scenes root node rather than building a new scene or opening a new
 window
*/
public class App extends Application implements Navigator {

    private static final int WINDOW_WIDTH = 640;
    private static final int WINDOW_HEIGHT = 760;

    private Stage stage;
    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        Screen splash = new SplashScreen(this);
        this.scene = new Scene(splash.getRoot(), WINDOW_WIDTH, WINDOW_HEIGHT);

        stage.setTitle("OOSD-Group20 Tetris");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
        stage.centerOnScreen();

        splash.onShow();
    }

    @Override
    public void show(Screen screen) {
        scene.setRoot(screen.getRoot());
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
