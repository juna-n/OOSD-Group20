package org.oosd.ui;

import javafx.scene.Parent;

public interface Screen {
    //root node of this screen
    Parent getRoot();

    /*
    Called every time the screen becomes visible. Screens that need to start
    an animation, focus a control or refresh data override this, the rest
    inherit the do-nothing default.
    */
    default void onShow() {
        // no-op by default
    }

    /*
    true if the window should shrink or grow to fit this screen's content,
    the game screen uses this because its size depends on field size and
    player count, every other screen uses the standard window size
    */
    default boolean sizesWindowToContent() {
        return false;
    }
}