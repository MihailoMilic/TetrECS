package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoresList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
/**
 * The ScoreScene class manages the display of scores in the game.
 */
public class ScoreScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger();
/**Remote Scores*/
    protected ListProperty<Pair<String, Integer>> remoteScores;
    /**Local Scores*/
    protected ListProperty<Pair<String, Integer>> localScores;
    private final Communicator communicator =new Communicator("ws://ofb-labs.soton.ac.uk:9700"); ;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final CountDownLatch newLeaderboardLatch = new CountDownLatch(1);
    private final StringProperty username = new SimpleStringProperty("");
    /**Game*/
    protected Game game;
    private final int score;
    private final BooleanProperty textInputShow = new SimpleBooleanProperty(false);
    TextField textfield = new TextField();
    ListProperty<String[]> scores;
    boolean singleplayer;
    /**
     * Makes Score Scene
     *
     * @param gameWindow gameWindow through which the scene is dispayed.
     * @param game The game object passed from the challange scene. */
    public ScoreScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        singleplayer = true;
        this.score = game.getScore().get();
        this.game = game;
        setupUsernameListeners();
        initializeLists();
        setupCommunicationsListener();

        logger.info("ScoreScene created");
    }
    /**
     * Makes a Score Scene
     *
     *
     *
     * @param gameWindow gameWindow through which the scene is dispayed.
     * @param game The game object passed from the challange scene.
     * @param scores Multiplayer game scores*/
    public ScoreScene(GameWindow gameWindow, Game game, ListView<String[]> scores){
        super(gameWindow);
        singleplayer = false;

        this.score = game.getScore().get();
        this.scores = new SimpleListProperty<>(scores.getItems());
        setupUsernameListeners();
        initializeLists();
        setupCommunicationsListener();

    }

    private void setupUsernameListeners() {
        username.addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if(singleplayer) {
                    writeLocalScores();
                }
                writeOnlineScore();
                username.set("");
                newLeaderboardLatch.countDown();
            }
        });
    }

    private void initializeLists() {
        remoteScores = new SimpleListProperty<>(FXCollections.observableArrayList());
        localScores = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    private void setupCommunicationsListener() {
        CommunicationsListener receiver = communication -> {
            logger.info("Received communication: " + communication);
            Platform.runLater(()->parseAndLoadScores(communication));
            countDownLatch.countDown();
            logger.info(countDownLatch.toString());
        };
        communicator.addListener(receiver);
    }

    private void parseAndLoadScores(String communication) {
        remoteScores.clear();
        String[] parts = communication.trim().replace("HISCORES", "").split("\n");
        for (String part : parts) {
            try {
                String name = part.split(":")[0].trim();
                int score = Integer.parseInt(part.split(":")[1].trim());
                remoteScores.add(new Pair<>(name, score));
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error("Invalid score format received: " + part);
            }
        }
    }

    @Override
    public void initialise() {
        loadRemoteScores();
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        setupRootKeyPressListener();
        var mainPane = new BorderPane();
        mainPane.setMaxSize(gameWindow.getWidth(), gameWindow.getHeight());
        root.getChildren().add(mainPane);
        //OK button
        var button = new Button("OK");
        button.setOnAction(e -> gameWindow.startMenu());
        button.getStyleClass().add("menu-button");
        mainPane.setBottom(button);

        //SCORES
        var scoresPane = new StackPane();
        scoresPane.setAlignment(Pos.CENTER);
        scoresPane.getStyleClass().add("menu-background");
        scoresPane.setMaxSize(gameWindow.getWidth(), gameWindow.getHeight());
        mainPane.setCenter(scoresPane);

        var mainScoresContainer = new VBox();
        var scoreContainer = new HBox();
        var score = new Label("YOUR SCORE: "+ this.score);
        score.getStyleClass().add("heading");
        mainScoresContainer.setAlignment(Pos.CENTER);
        mainScoresContainer.getChildren().addAll(score, scoreContainer);
        scoreContainer.setAlignment(Pos.CENTER);
        if (singleplayer) {
            scoreContainer.getChildren().addAll(createScoresList(remoteScores), createScoresList(localScores));
        }else
            scoreContainer.getChildren().addAll(createScoresList(remoteScores), createScoresList1(scores));
        scoresPane.getChildren().add(mainScoresContainer);

        setupTextInput();
        logger.info("Remote scores loaded: {}", remoteScores.size());
        writeOnlineScore();
        if(singleplayer) {
            writeLocalScores();
        }
    }

    private void setupRootKeyPressListener() {
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                gameWindow.startMenu();
            }
        });
    }

    private void setupTextInput(){
        textInputShow.addListener((observable, oldValue, newValue)->{
            if(newValue && username.get().isEmpty()){
                textfield.setPromptText("Your Name Warrior..");
                textfield.setOnAction(actionEvent -> {
                    username.set(textfield.getText());
                    textInputShow.set(false);
                });
                root.getChildren().add(textfield);
            }else{
                root.getChildren().remove(textfield);
            }
        });
    }


    private ScoresList<Pair<String,Integer>> createScoresList(ListProperty<Pair<String, Integer>> scores) {
        ScoresList<Pair<String,Integer>> scoresList = new ScoresList<>();
        scoresList.setupListView(lv ->new ListCell<>(){
            @Override
            protected void updateItem(Pair<String, Integer> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getKey() + ": " + item.getValue());
                }
            }
        });
        scoresList.setItems(scores);
        return scoresList;
    }
    private ScoresList<String[]> createScoresList1(ListProperty<String[]> scores) {
        ScoresList<String[]> scoresList = new ScoresList<>();
        scoresList.setupListView(lv ->new ListCell<>(){
            @Override
            protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item[0] + ": " + item[1]);
                }
            }
        });
        scoresList.setItems(scores);
        return scoresList;
    }

    private void loadRemoteScores() {
        communicator.send("HISCORES");
    }
/**Writes online score.*/
    public void writeOnlineScore() {
        if (!remoteScores.isEmpty()) {
            for (Pair<String, Integer> remoteScore : remoteScores) {
                if (score > remoteScore.getValue()) {
                    if (username.get().isEmpty()){
                        textInputShow.set(true);
                    }else{
                        remoteScores.add(new Pair<>(username.get(), score));
                    }
                    break;
                }
            }
        }
        remoteScores.sort((o1,o2)->o2.getValue()-o1.getValue());
    }
    private ArrayList<Pair<String, Integer>> helperFileReader() {
        ArrayList<Pair<String, Integer>> list = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("localscores.txt"));
            for (String line : lines) {
                String[] l = line.trim().split(":");
                list.add(new Pair<>(l[0], Integer.parseInt(l[1])));
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return list;
    }

    /**
     *Adds the new score to the local scores if it beats any of the previous ones and writes the new list to a file.
     * */
    public void writeLocalScores() {
        localScores.clear();
        ArrayList<Pair<String, Integer>> oldScores = helperFileReader();
        localScores.addAll(oldScores);
        boolean contains = false;
        for (var pair: localScores){
            if(pair.getValue() ==score) {
                contains = true;
                break;
            }
        }
        if(contains){
            logger.info("Already contains");
        } else if (localScores.isEmpty()) {
            if(!Objects.equals(username.get(), "")){
                localScores.add(new Pair<>(username.get(), score));
                try (FileWriter writer = new FileWriter("localscores.txt", false)) {
                    writer.write(username.get() + ":" + score + "\n");
                } catch (IOException e) {
                    logger.error(e);
                }
                }else{
                    textInputShow.set(true);
                }
        }
        else {
            if(!Objects.equals(username.get(), "")){
                boolean scoreAdded = false;
                List<Pair<String, Integer>> toAdd = new ArrayList<>();
                for (var pair : localScores) {
                    if (pair.getValue() == score) {
                        logger.info("already present");
                        break;
                    }
                    if (pair.getValue() < score && !scoreAdded) {
                        toAdd.add(new Pair<>(username.get(), score));
                        scoreAdded = true;
                    }
                }

                localScores.addAll(toAdd);

                localScores.sort((o1, o2) -> o2.getValue() - o1.getValue());
                logger.info(localScores.toString());
                try (FileWriter writer = new FileWriter("localscores.txt", false)) {
                    for (var pair : localScores) {
                        writer.write(pair.getKey() + ":" + pair.getValue() + "\n");
                    }
                } catch (IOException e) {
                    logger.error(e);
                }
            }else{
                for(var pair: localScores){
                    if(pair.getValue() <score) {
                        textInputShow.set(true);
                        break;
                    }
                }
            }
        }
    }
}
