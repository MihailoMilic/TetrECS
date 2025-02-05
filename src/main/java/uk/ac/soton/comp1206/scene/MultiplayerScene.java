package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.MultiPlayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Leaderboard;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
/**Multiplayer Scene*/
public class MultiplayerScene extends ChallengeScene{
    ListProperty<String[]> leaderboardStats = new SimpleListProperty<>(FXCollections.observableArrayList());
    Leaderboard leaderboard = new Leaderboard();
    private final Communicator communicator;
    ListProperty<Pair<String,String>> channelMessages;
    MultiPlayerGame multiPlayerGame;
    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     * @param communicator communicator for communication
     */
    public MultiplayerScene(GameWindow gameWindow, Communicator communicator) {
        super(gameWindow);
        this.communicator = communicator;
        communicator.send("START");
        leaderboard.setItems(leaderboardStats);
         channelMessages = new SimpleListProperty<>(FXCollections.observableArrayList());
        CommunicationsListener listener = communication -> {
            String type = communication.split(" ")[0];
            switch (type){

                case "ERROR"-> logger.error(communication.trim());

                case "MSG" ->{
                    String[] rawMessage = communication.replace("MSG","").trim().split(":");
                    channelMessages.add(new Pair<>(rawMessage[0], rawMessage[1]));
                }
                case "NICK" -> communicator.send("USERS");
                case "SCORES"->{
                    Platform.runLater(()->leaderboardStats.clear());
                    String[] playersStat = communication.replace("SCORES","").trim().split("\n");
                    for (String playerStat : playersStat) {
                        Platform.runLater(()->leaderboardStats.add(playerStat.trim().split(":")));
                    }
                }

            }
        };
        communicator.addListener(listener);
    }
    @Override
    public void build(){
        var leftContainer = new VBox();
        var textContainer = new VBox();
        var textbox = new TextArea();
        textbox.setFocusTraversable(false);
        textbox.setEditable(false);
        textbox.setFocusTraversable(false);
        textbox.setOnMouseClicked(actionEvent -> mainPane.requestFocus());
        this.channelMessages.addListener((ob,o,n)->{
            textbox.clear();
            for (var pair :channelMessages ){
                textbox.appendText(pair.getKey()+":\n"+pair.getValue()+"\n\n");
            }
        });
        var sendMessage = new TextField();
        sendMessage.setPromptText("Send a Message");
        sendMessage.setOnAction(actionEvent -> {
            communicator.send("MSG "+sendMessage.getText());
            mainPane.requestFocus();
            sendMessage.setText("");

        });
        textContainer.getChildren().addAll(textbox, sendMessage);
        textContainer.setMaxWidth(200);
        leftContainer.getChildren().addAll(leaderboard,textContainer);
        setUpLeaderBaord(leftContainer);
        super.build();
    }
    @Override
    public void setupGame(){
        communicator.send("SCORES");
        logger.info("Starting a new multiplayer challange");
        super.setupGame();
        multiPlayerGame.getScore().addListener((ob,o,n)-> {
            communicator.send("SCORE " + n);
            communicator.send("SCORES");
            updateLeaderboard();

        });
        multiPlayerGame.getLives().addListener((ob,o,n)-> {
            communicator.send("LIVES " + n);
            communicator.send("SCORES");
            updateLeaderboard();
        });
    }

    @Override
    public void setGame(){
        multiPlayerGame = new MultiPlayerGame(5,5, communicator);
        super.game = multiPlayerGame;
        game.setNextPieceListener(this);
        game.initialisePrimaryPieces();
    };
    @Override
    public void onBack() {
        communicator.send("DIE");
        super.onBack();
    }
    private void updateLeaderboard(){
        Platform.runLater(()-> leaderboard.setupListView(lv ->new ListCell<>(){
            @Override
            protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if(!item[2].equals("DEAD")) {
                        setText(item[0] + ": " + item[1]);
                    }else{
                        setText(item[0]);

                        setStyle("-fx-strikethrough: true");
                    }
                }
            }
        }));
    }

    @Override
    public void showScores(){
        communicator.send("DIE");
        challangeMusic.setVolume(.1);
        challangeMusic.stopAudio();
        Platform.runLater(()->gameWindow.startScores(game, leaderboard.getListView()));
    }


}
