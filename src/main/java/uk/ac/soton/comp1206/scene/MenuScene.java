package uk.ac.soton.comp1206.scene;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    MultiMedia menuMusic = new MultiMedia("src/main/resources/music/menu.mp3");
    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);
        menuMusic.playMusic();
        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);
        //Awful title
        var title = new Text("TetrECS");
        title.getStyleClass().add("title");



        //For now, let us just add a button that starts the game. I'm sure you'll do something way better.
        var button = new Button("Play");
        var insutrctions = new Button("Instructions");
        var multiplayer = new Button("Multiplayer");
        insutrctions.getStyleClass().add("menuItem");
        button.getStyleClass().add("menuItem");
        multiplayer.getStyleClass().add("menuItem");

        var menu = new VBox();
        menu.setAlignment(Pos.CENTER);
        menu.getChildren().addAll(title, button, insutrctions, multiplayer);
        menu.setSpacing(60);
        mainPane.setCenter(menu);
        //Bind the button action to the startGame method in the menu
        button.setOnAction(this::startGame);
        insutrctions.setOnAction(this::startInstructions);
        multiplayer.setOnAction(this::startMultiplayer);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {

    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
        menuMusic.stopMusic();
    }
    /**
     * Starts instructions scene.*/
    private void startInstructions(ActionEvent event){gameWindow.startInstructions();}
/**
 * Starts multiplayer scene*/
    private void startMultiplayer(ActionEvent event) {gameWindow.startMultiplayer();
    menuMusic.stopMusic();}
}
