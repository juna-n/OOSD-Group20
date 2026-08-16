package org.oosd.ui;
/*
 screens are handed a navigator instead of a stage. That way no screen can
 reach into the window and resize it, retitle it or close it by accident
*/
public interface Navigator {
    //Replaces the currently visible screen
    void show(Screen screen);

    //shuts down the application
    void exitApp();
}
