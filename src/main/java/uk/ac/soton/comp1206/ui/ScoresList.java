package uk.ac.soton.comp1206.ui;

import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * The ScoresList class represents a custom VBox containing a ListView for displaying scores.
 *
 * @param <T> the type of items in the ListView
 */
public class ScoresList<T> extends VBox {

    /** The ListView component for displaying scores. */
    protected ListView<T> listView = new ListView<>();

    /**
     * Constructs a ScoresList object.
     */
    public ScoresList() {
        this.getChildren().add(listView);
        super.setAlignment(Pos.CENTER);
        super.setSpacing(10);
        super.setPadding(new Insets(10, 10, 10, 10));
        listView.getStyleClass().add("scorelist");
        reveal();
    }

    /**
     * Sets up the ListView with a custom cell factory.
     *
     * @param cellFactory the cell factory for customizing list cell appearance
     */
    public void setupListView(Callback<ListView<T>, ListCell<T>> cellFactory) {
        listView.setCellFactory(cellFactory);
    }

    /**
     * Binds the items of the ListView to the specified ListProperty.
     *
     * @param items the ListProperty containing items to be displayed
     */
    public void setItems(ListProperty<T> items) {
        listView.itemsProperty().bind(items);
    }

    /**
     * Animates the reveal effect for the ScoresList.
     */
    public void reveal() {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), this);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }
}
