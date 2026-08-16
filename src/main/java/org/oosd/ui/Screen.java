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
}
