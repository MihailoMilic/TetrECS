package uk.ac.soton.comp1206.ui;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
/**
 *Leaderboard is a custom ui component for displaying ingame leaderboard of players and their scores.
 * */
public class Leaderboard extends ScoresList<String[]> {
    /**Initialises a new Leaderbaord*/
    public Leaderboard() {
        super();
        super.listView.setCellFactory(listView -> new ListCell<String[]>() {
            @Override
            protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item[2].equals("DEAD")) {
                    setText(item[0] +": " + item[2]);

                } else {
                    setText(item[0] + ": " + item[1]);
                    setStyle(null); // Clear any previous style
                }
            }
        });
        setMaxWidth(200);
        setFocusTraversable(false);
        setFocused(false);
    }
    /**
     * Returns a list of the scores from this leaderboard.
     *
     *
     * @return listView
     * */
    public ListView<String[]> getListView() {
        return listView;
    }
}
