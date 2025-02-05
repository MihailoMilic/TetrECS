package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.KeyboardNavigateListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Leaderboard;

import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.List;
/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements NextPieceListener,GameLoopListener {

    static final Logger logger = LogManager.getLogger(MenuScene.class);
    /**
     * Game object.*/
    protected Game game;
    /**
     * Timeline width*/
    final protected FloatProperty width;
    /**
     * Color object used for timebar UI*/
    final protected ObjectProperty<Color> color;
    KeyboardNavigateListener keyboardNavigateListener;
    PieceBoard pieceBoard;
    Timeline  timeline = new Timeline();
    Rectangle timeBar;
    PieceBoard followingPieceBoard;
    /**
     * High score*/
    final protected IntegerProperty highscore = new SimpleIntegerProperty(getHighScore());
    final BorderPane mainPane = new BorderPane();

    //sounds
    MultiMedia rotateSound = new MultiMedia("src/main/resources/sounds/rotate.wav");
    MultiMedia placeSound = new MultiMedia("src/main/resources/sounds/place.wav");
    MultiMedia lifeloseSound = new MultiMedia("src/main/resources/sounds/lifelose.wav");
    MultiMedia messageSound = new MultiMedia("src/main/resources/sounds/message.wav");
    MultiMedia levelSound = new MultiMedia("src/main/resources/sounds/level.wav");
    MultiMedia explodeSound = new MultiMedia("src/main/resources/sounds/explode.wav");
    MultiMedia clearSound = new MultiMedia("src/main/resources/sounds/clear.wav");
    MultiMedia plingSound = new MultiMedia("src/main/resources/sounds/pling.wav");
    MultiMedia intro = new MultiMedia("src/main/resources/sounds/intro.mp3");

    MultiMedia challangeMusic = new MultiMedia("src/main/resources/music/challange.mp3");


    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        width = new SimpleFloatProperty((float) gameWindow.getWidth() /2);
        color = new SimpleObjectProperty<>(Color.WHITE);
        logger.info("Creating Challenge Scene");
        challangeMusic.playMusic();
        challangeMusic.setVolume(.5);
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setFocusTraversable(true);
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);


        challengePane.getChildren().add(mainPane);
        var board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        game.setLinesClearedListener(board);
        board.setStyle("-fx-border-radius: 5");
        mainPane.setCenter(board);

        var score = new TextField();
        var level = new TextField();
        var lives = new TextField();
        var multiplier = new TextField();
        var highscores = new TextField();
        textStyle(score, level, lives, multiplier, highscores);


        var simpleInfo  = new HBox();
        var rightY = new VBox();

        simpleInfo.getChildren().addAll(score, level, lives, multiplier, highscores);
        simpleInfo.setSpacing(20);
        simpleInfo.setStyle("-fx-padding:20");
        simpleInfo.setAlignment(Pos.CENTER);

        if(followingPieceBoard != null && pieceBoard != null) {

            rightY.getChildren().addAll(followingPieceBoard, pieceBoard);
        }
        rightY.setStyle("-fx-padding:20");
        rightY.setSpacing(40);
        rightY.setAlignment(Pos.CENTER);

        mainPane.setTop(simpleInfo);
        mainPane.setRight(rightY);
        mainPane.setBottom(timeBar);

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        board.setOnBlockHover(this::hover);

        challengePane.setOnKeyPressed(keyEvent -> {
            challengePane.requestFocus();
                switch (keyEvent.getCode()) {
                    case SPACE, R-> {
                        plingSound.stopAudio();
                        plingSound.playAudio();
                        swapPiece();
                    }
                    case ESCAPE-> {
                        onBack();
                    }
                    case OPEN_BRACKET,Q,Z-> {
                        rotateSound.stopAudio();
                        rotateSound.playAudio();
                        rotateLeft();

                    }
                    case E,C,CLOSE_BRACKET-> {
                        rotateSound.stopAudio();
                        rotateSound.playAudio();
                        rotateRight();
                    }
                    case UP, W-> {
                        keyboardNavigateListener.up();
                        int[] c = keyboardNavigateListener.coordinate();
                        board.unHighlightAll();
                        board.setHighlighted(c[0], c[1], true);
                    }
                    case DOWN,S-> {
                        keyboardNavigateListener.down();
                        int[] c = keyboardNavigateListener.coordinate();
                        board.unHighlightAll();
                        board.setHighlighted(c[0], c[1], true);
                    }
                    case LEFT,A-> {
                        keyboardNavigateListener.left();
                        int[] c = keyboardNavigateListener.coordinate();
                        board.unHighlightAll();
                        board.setHighlighted(c[0], c[1], true);
                    }
                    case RIGHT,D-> {
                        keyboardNavigateListener.right();
                        int[] c = keyboardNavigateListener.coordinate();
                        board.unHighlightAll();
                        board.setHighlighted(c[0], c[1], true);
                    }

                    case ENTER, X-> {
                        placeSound.stopAudio();
                        placeSound.playAudio();
                        keyboardNavigateListener.press();
                    }
                    default -> {
                    }
                }
        });

    }
    void setUpLeaderBaord(Node node){
        mainPane.setLeft(node);
    }

    private void textStyle(TextField score, TextField level, TextField lives, TextField multiplier, TextField highscores){
        score.textProperty().bind(game.getScore().asString());
        score.setStyle("-fx-border-radius: 5");
        score.setEditable(false);
        score.setAlignment(Pos.CENTER);

        level.setFocusTraversable(false);
        level.textProperty().bind(game.getLevel().asString());
        level.setStyle("-fx-border-radius: 5");
        level.setEditable(false);
        level.setAlignment(Pos.CENTER);

        lives.setFocusTraversable(false);
        lives.textProperty().bind(game.getLives().asString());
        lives.setStyle("-fx-border-radius: 5");
        lives.setEditable(false);
        lives.setAlignment(Pos.CENTER);

        multiplier.setFocusTraversable(false);
        multiplier.textProperty().bind(game.getMultiplier().asString());
        multiplier.setStyle("-fx-border-radius: 5");
        multiplier.setEditable(false);
        multiplier.setAlignment(Pos.CENTER);

        multiplier.setFocusTraversable(false);
        multiplier.textProperty().bind(game.getMultiplier().asString());
        multiplier.setStyle("-fx-border-radius: 5");
        multiplier.setEditable(false);
        multiplier.setAlignment(Pos.CENTER);

        highscores.setFocusTraversable(false);
        highscores.textProperty().bind(highscore.asString());
        highscores.setStyle("-fx-border-radius: 5");
        highscores.setEditable(false);
        highscores.setAlignment(Pos.CENTER);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    void blockClicked(GameBlock gameBlock) {
        placeSound.stopAudio();
        placeSound.playAudio();
        game.blockClicked(gameBlock);

    }
    void hover(GameBlock gameBlock, GameBoard board){
        game.setCoordinate(gameBlock.getX(), gameBlock.getY());
        int[] c = game.coordinate();
        board.setHighlighted(c[0], c[1], true);
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        setGame();
        pieceBoard =  new PieceBoard(game.getNextPieceGrid(), (double) gameWindow.getWidth() /5, (double) gameWindow.getWidth() /5);
        followingPieceBoard =  new PieceBoard(game.getFollowingPieceGrid(), (double) gameWindow.getWidth() /10, (double) gameWindow.getWidth() /10);
        pieceBoard.setHighlighted(1, 1, true);
        pieceBoard.setOnMouseClicked(mouseEvent -> rotateRight());
        followingPieceBoard.setOnMouseClicked(mouseEvent -> swapPiece());
        //nextPiece(game.getCurrentPiece(), game.getFollowingPiece());

        keyboardNavigateListener = game;
        game.setGameLoopListener(this);
        game.getScore().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.intValue() > highscore.get()) {
                highscore.set(newValue.intValue());
            }
        });
        timeBar = new Rectangle();
        timeBar.setWidth( gameWindow.getWidth());
        timeBar.setHeight(30);
        var duration = Duration.millis(game.getTimeDelay().get());

        var initialWidth = gameWindow.getWidth();
        var finalWidth = 0;

        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(timeBar.widthProperty(), initialWidth)),
                new KeyFrame(Duration.ZERO, new KeyValue(timeBar.fillProperty(), Color.WHITE)),
                new KeyFrame(Duration.millis((double) game.getTimeDelay().get()/2), new KeyValue(timeBar.fillProperty(), Color.YELLOW)),
                new KeyFrame(Duration.millis((double) 3*game.getTimeDelay().get()/4), new KeyValue(timeBar.fillProperty(), Color.RED)),
                new KeyFrame(duration, new KeyValue(timeBar.widthProperty(), finalWidth))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        game.getTimeDelay().addListener((observable, oldValue, newValue) -> {
            System.out.println("Listener triggered, new value: " + newValue);
            timeline.stop();
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(timeBar.widthProperty(), initialWidth)),
                    new KeyFrame(Duration.ZERO, new KeyValue(timeBar.fillProperty(), Color.WHITE)),
                    new KeyFrame(Duration.millis((double) newValue.longValue()/2), new KeyValue(timeBar.fillProperty(), Color.YELLOW)),
                    new KeyFrame(Duration.millis((double) 3*newValue.longValue()/4), new KeyValue(timeBar.fillProperty(), Color.RED)),
                    new KeyFrame(Duration.millis(newValue.longValue()), new KeyValue(timeBar.widthProperty(), finalWidth))
                );
            timeline.play();
        });
    }

    /**
     * Helper function used to allow child class MultiplayerScene to set MultiplayerGame as game field.
     */
    protected void setGame(){
        this.game = new Game(5,5);
        game.setNextPieceListener(this);
        game.initialisePrimaryPieces();
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
    }

    /**
     *
     * @param piece the current piece
     * @param followingPiece the following piece
     * Displays the current and following piece onto the UI.
     */
    @Override
    public void nextPiece(GamePiece piece, GamePiece followingPiece){
        if(pieceBoard==null || followingPieceBoard==null){
            pieceBoard =  new PieceBoard(game.getNextPieceGrid(), (double) gameWindow.getWidth() /5, (double) gameWindow.getWidth() /5);
            followingPieceBoard =  new PieceBoard(game.getFollowingPieceGrid(), (double) gameWindow.getWidth() /10, (double) gameWindow.getWidth() /10);
            pieceBoard.display(piece);
            followingPieceBoard.display(followingPiece);
        }else{
            pieceBoard.display(piece);
            followingPieceBoard.display(followingPiece);
        }

    }

    /**
     * Navigation function used for exiting game.
     */
    public void onBack(){
        game.stopGame();
        gameWindow.startMenu();
        challangeMusic.setVolume(.1);
        challangeMusic.stopMusic();
    }

    /**
     * Swap current and following piece.
     */
    public void swapPiece() {
        game.swapCurrentPiece();
        pieceBoard.display(game.getCurrentPiece());
        followingPieceBoard.display(game.getFollowingPiece());
    }
    /**
     * Rotate current piece left.
     */
    void rotateLeft(){
        game.rotatePiece(3);
        pieceBoard.display(game.getCurrentPiece());
    }

    /**
     * Rotate current piece right.
     */
    void rotateRight(){
        game.rotatePiece();
        pieceBoard.display(game.getCurrentPiece());
    }

    /**
     * Implemented GameLoopListener method used to start the timebar on the UI.
     */
    @Override
    public void start() {
        logger.info("Starting a new bar");
        timeline.play();
        width.set((float) gameWindow.getWidth() /2);
        color.set(Color.WHITE);
    }
    /**
     * Implemented GameLoopListener method used to stop the timebar on the UI.
     */
    @Override
    public void stop() {
        timeline.stop();
        width.set(0);

    }
    /**
     * Implemented GameLoopListener method used to end the game.
     */
    @Override
    public void endGame(){
        timeline.stop();
    }
    /**
     * Implemented GameLoopListener method used to start the scores scene.
     */
    @Override
    public void showScores(){
        challangeMusic.setVolume(.1);
        challangeMusic.stopAudio();
        Platform.runLater(()->gameWindow.startScores(game));
    }

    /**
     * Scans the local scores file and returns the highest score to beat by the player.
     * @return int highest score
     */
    private int getHighScore(){
        int highScore = 0;
        try {
            List<String> lines = Files.readAllLines(Paths.get("localscores.txt"));
            if (!lines.isEmpty()){
                String l = lines.get(0);
                highScore = Integer.parseInt(l.split(":")[1].trim());
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return highScore;
    }

}
