package org.oosd.ui;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import org.oosd.model.HighScore;

import java.util.Optional;

//shared dialogs, the exit prompt and the back to menu prompt have to look and behave the same
public final class Dialogs {

    private Dialogs() {
        //utility class
    }

    public static boolean confirm(Node owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        attachTo(alert, owner);

        return alert.showAndWait()
                .filter(response -> response == ButtonType.YES)
                .isPresent();
    }

    /*
    asks for a player name for the high score table
    empty if the player cancelled, OK is disabled until something is typed
    and the field won't accept more than HighScore.MAX_NAME_LENGTH characters
    */
    public static Optional<String> askName(Node owner, String title, String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(message);
        dialog.setContentText("Name:");
        attachTo(dialog, owner);

        TextField field = dialog.getEditor();
        field.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= HighScore.MAX_NAME_LENGTH ? change : null));

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> field.getText().isBlank(), field.textProperty()));

        return dialog.showAndWait()
                .map(String::strip)
                .filter(name -> !name.isEmpty());
    }

    //centres the dialog over the game window and gives it the shared stylesheet
    private static void attachTo(Dialog<?> dialog, Node owner) {
        Styles.applyTo(dialog.getDialogPane());
        if (owner != null && owner.getScene() != null) {
            dialog.initOwner(owner.getScene().getWindow());
        }
    }
}