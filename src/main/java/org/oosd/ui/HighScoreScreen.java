package org.oosd.ui;

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
import javafx.scene.layout.VBox;
import org.oosd.model.HighScore;
import org.oosd.model.HighScoreStore;

//display the top 10 scores
public class HighScoreScreen extends BaseScreen {

    public HighScoreScreen(Navigator navigator) {
        super(navigator);
    }

    @Override
    protected Parent buildRoot() {
        Label heading = new Label("High Scores");
        heading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f2f2f2;");

        TableView<HighScore> table = new TableView<>(
                FXCollections.observableArrayList(HighScoreStore.topTen()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(340);
        table.setMaxWidth(420);
        table.setPlaceholder(new Label("No scores recorded yet"));

        //rank is the rows position in the table, not a field on HighScore
        //so it comes from the cell's own index rather than from the data.
        TableColumn<HighScore, Void> rankColumn = new TableColumn<>("Rank");
        rankColumn.setSortable(false);
        rankColumn.setMaxWidth(70);
        rankColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        TableColumn<HighScore, String> nameColumn = new TableColumn<>("Player");
        nameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().playerName()));

        TableColumn<HighScore, Integer> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().score()));

        table.getColumns().add(rankColumn);
        table.getColumns().add(nameColumn);
        table.getColumns().add(scoreColumn);

        Button backButton = new Button("Back");
        backButton.setPrefWidth(200);
        backButton.setPrefHeight(40);
        backButton.setOnAction(event -> navigator.show(new MainMenuScreen(navigator)));

        VBox layout = new VBox(24, heading, table, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #1b1b22;");
        return layout;
    }
}
