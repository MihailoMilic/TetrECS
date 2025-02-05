package uk.ac.soton.comp1206.scene;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Box;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**Instructions scene dynamically displays all the pieces for players to get familiar with the game.*/
public class InstructionsScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger();
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating instructions scene");
    }

    @Override
    public void initialise() {

    }

    /**
     * On click displays the piece you want on the board
     *
     *
     * @param board board you want the peace displayed on.
     * @param piece piece you want displayed.
     * @return eventHandler that places the piece on the board.
     * */
    public EventHandler<ActionEvent> onClick(PieceBoard board, int piece){
        return event -> board.display(GamePiece.createPiece(piece));
    }
    @Override
    public void build() {

        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        var instructionsPane = new StackPane();
        instructionsPane.setMaxWidth(gameWindow.getWidth());
        instructionsPane.setMaxHeight(gameWindow.getHeight());
        instructionsPane.getStyleClass().add("menu-background");
        root.getChildren().add(instructionsPane);
        root.requestFocus();

        var mainPane = new BorderPane();
        instructionsPane.getChildren().add(mainPane);

        var instructionsBoard = new PieceBoard(new Grid(3,3),gameWindow.getWidth()/3,gameWindow.getWidth()/3);
        instructionsPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                // Handle Esc key press here
                onBack();
            }
        });
        instructionsPane.requestFocus();
        var dynamicButtons = new VBox();
        var backButton = new Button("Back");
        backButton.getStyleClass().add("mediumMenuItem");
        backButton.setOnAction(this::onBack);
        dynamicButtons.setSpacing(5);
        dynamicButtons.setAlignment(Pos.CENTER);
        for (int i=1; i<15;i++){
            var button  = new Button(String.valueOf(i));
            button.setOnAction(onClick(instructionsBoard, i));
            button.getStyleClass().add("smallMenuItem");
            dynamicButtons.getChildren().add(button);


        }
        var box = new HBox();
        box.getChildren().add(backButton);
        instructionsBoard.setStyle("-fx-border-radius: 5");
        mainPane.setCenter(instructionsBoard);
        mainPane.setRight(dynamicButtons);
        mainPane.setTop(box);


    }
    /**
     * Sends the user to the menu scene
     *
     * @param event action Event
     * */
    public void onBack(ActionEvent event){
        gameWindow.startMenu();
    }
    /**
     * Sends the user to the menu scene*/
    public void onBack(){
        gameWindow.startMenu();
    }
}
