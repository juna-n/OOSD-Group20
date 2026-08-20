package org.oosd.ui;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

//shared dialogs, the exit prompt and the back to menu prompt have to look and behave the same
public final class Dialogs {

    private Dialogs() {
        //utility class
    }

    public static boolean confirm(Node owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);

        if (owner != null && owner.getScene() != null) {
            alert.initOwner(owner.getScene().getWindow());
        }

        return alert.showAndWait()
                .filter(response -> response == ButtonType.YES)
                .isPresent();
    }
}
