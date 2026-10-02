package org.oosd.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.oosd.model.HighScore;
import org.oosd.persistence.HighScoreManager;

//display the top 10 scores, loaded from and cleared in the JSON score file
public class HighScoreScreen extends BaseScreen {

    private final HighScoreManager scores = HighScoreManager.getInstance();

    private TableView<HighScore> table;

    public HighScoreScreen(Navigator navigator) {
        super(navigator);
    }

    @Override
    protected Parent buildRoot() {
        Label heading = styledLabel("High Scores", "heading");

        table = new TableView<>(FXCollections.observableArrayList(scores.topTen()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(340);
        table.setMaxWidth(520);
        table.setPlaceholder(new Label("No scores recorded yet"));

        //rank is the rows position in the table, not a field on HighScore
        //so it comes from the cell's own index rather than from the data.
        TableColumn<HighScore, Void> rankColumn = new TableColumn<>("Rank");
        rankColumn.setSortable(false);
        rankColumn.setMaxWidth(60);
        rankColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        TableColumn<HighScore, String> nameColumn = new TableColumn<>("Player");
        nameColumn.setSortable(false);
        nameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().playerName()));

        TableColumn<HighScore, Integer> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setSortable(false);
        scoreColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().score()));

        TableColumn<HighScore, String> configColumn = new TableColumn<>("Config");
        configColumn.setSortable(false);
        configColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().configSummary()));

        table.getColumns().add(rankColumn);
        table.getColumns().add(nameColumn);
        table.getColumns().add(scoreColumn);
        table.getColumns().add(configColumn);

        Button clearButton = new Button("Clear Scores");
        clearButton.getStyleClass().add("wide-button");
        //nothing to clear when the table is already empty
        clearButton.disableProperty().bind(Bindings.isEmpty(table.getItems()));
        clearButton.setOnAction(event -> confirmClear());

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("wide-button");
        backButton.setOnAction(event -> navigator.show(new MainMenuScreen(navigator)));

        HBox buttons = new HBox(16, clearButton, backButton);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(24, heading, table, buttons);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getStyleClass().add("screen");
        return layout;
    }

    private void confirmClear() {
        if (Dialogs.confirm(getRoot(), "Clear High Scores",
                "Delete every high score? This cannot be undone.")) {
            scores.clear();
            table.getItems().setAll(scores.topTen());
        }
    }
}