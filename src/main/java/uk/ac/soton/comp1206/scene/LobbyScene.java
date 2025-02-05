package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
/**Lobby Scene*/
public class LobbyScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger();
    ListProperty<String> channels;
    private final Communicator communicator;
    BooleanProperty modalShow;
    BooleanProperty channelShow;
    ListProperty<String> players;
    BooleanProperty isHost;
    StringProperty currentChannel;
    ListProperty<Pair<String,String>> channelMessages;
    BooleanProperty namePrompt;
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        namePrompt = new SimpleBooleanProperty(false);
        modalShow = new SimpleBooleanProperty(false);
        channelShow = new SimpleBooleanProperty(false);
        isHost = new SimpleBooleanProperty(false);
        currentChannel = new SimpleStringProperty("");
        channelMessages = new SimpleListProperty<>(FXCollections.observableArrayList());
        ObservableList<String> list = FXCollections.observableArrayList();
        this.channels = new SimpleListProperty<>(list);
        this.communicator = new Communicator("ws://ofb-labs.soton.ac.uk:9700");
        this.players = new SimpleListProperty<>(FXCollections.observableArrayList());
        Timer timer = new Timer();
        //Handling what is recieved from the communicator
        CommunicationsListener listener = communication -> {
            String type = communication.split(" ")[0];
            switch (type){
                case "CHANNELS"-> {
                    channels.clear();
                    String[] rawChannels = communication.replace("CHANNELS","").trim().split("\n");
                    for (String channel : rawChannels) {
                        channels.add(channel.trim());
                    }
                }
                case "ERROR"-> logger.error(communication.trim());
                case "JOIN"-> {
                    communicator.send(communication);
                    logger.info("Channel joined. \n " + communication.trim());
                    channelShow.set(true);
                    isHost.set(false);

                }
                case "USERS"->{
                    String[] users = communication.replace("USERS","").trim().split("\n");
                    Platform.runLater(()->{
                        players.clear();
                        for (String user : users) {
                            players.add(user.trim());
                        }
                    });
                }case "HOST"-> isHost.set(true);
                case "PARTED"-> Platform.runLater(()-> {
                    logger.info("You left the channel");
                    channelShow.set(false);
                    channelMessages.clear();
                });
                case "MSG" ->{
                    String[] rawMessage = communication.replace("MSG","").trim().split(":");
                    channelMessages.add(new Pair<>(rawMessage[0], rawMessage[1]));
                }
                case "NICK" -> communicator.send("USERS");
                case "START" -> {
                    if(!isHost.get()){
                        Platform.runLater(() -> gameWindow.startMultiPlayerScene(communicator));
                    }
                }

            }
        };
        communicator.addListener(listener);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                channels.clear();
                communicator.send("LIST");
            }
        },0,1000 );
        communicator.send("LIST");
    }

    @Override
    public void initialise() {

    }

    @Override
    public void build() {
        communicator.send("SCORES");
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        var lobbyPane = new StackPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("menu-background");
        root.getChildren().add(lobbyPane);
        ListView<String> channels = new ListView<>();
        channels.itemsProperty().bind(this.channels);
        var mainPane = new BorderPane();
        lobbyPane.getChildren().add(mainPane);
        var container = new FlowPane();
        container.setAlignment(Pos.CENTER);
        container.setHgap(50);
        container.setVgap(50);
        container.setBlendMode(BlendMode.DIFFERENCE);
        var mainContainer = new BorderPane();
        mainPane.setCenter(mainContainer);
        mainContainer.setCenter(container);
        //dynamically add channels
        this.channels.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(()->{
                container.getChildren().clear();
                ArrayList<String> channelCopy = new ArrayList<>(channels.getItems());
                ArrayList<Node> tobeAdded = new ArrayList<>();
                for(String channel : channelCopy) {
                    var box = new VBox();
                    box.setBorder(Border.stroke(Color.PURPLE));
                    box.setMinWidth(100);
                    box.setAlignment(Pos.CENTER);
                    box.onMouseClickedProperty().set(mouseEvent -> {
                        communicator.send("JOIN " + channel);
                        currentChannel.set(channel);
                    });
                    var label = new Label(channel);
                    label.setTextFill(Paint.valueOf(String.valueOf(Color.WHITE)));
                    label.getStyleClass().add("channelLabel");
                    box.getChildren().add(label);
                    box.setMaxWidth(100);
                    box.setMaxHeight(100);
                    tobeAdded.add(box);
                }
                container.getChildren().addAll(tobeAdded);
            });
        });
        //Channel name prompt
        this.modalShow.addListener((observable, oldValue, newValue) -> {
            if(newValue){
                var modal = new VBox();
                var input = new TextField();
                modal.setAlignment(Pos.CENTER);
                input.setPromptText("Channel Name");
                input.getStyleClass().add("textInput");
                input.setMaxWidth(150);
                input.setOnAction(actionEvent -> {
                    logger.info("Input recieved:{}", input.getText());
                    communicator.send("CREATE "+input.getText());
                    communicator.send("LIST");
                    currentChannel.set(input.getText());
                    this.modalShow.set(false);
                });
                modal.getChildren().addAll(input);
                mainPane.setCenter(modal);
            }else mainPane.setCenter(mainContainer);
        });
        //Individual channel's window
        this.channelShow.addListener((observable, oldValue, newValue) -> {
            Timer channelTimer = new Timer();
            if(newValue){
                channelTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(()-> communicator.send("USERS"));
                    }
                }, 5000, 5000);
                var channelContainer = new BorderPane();
                var channel = new Text();
                channel.textProperty().bind(this.currentChannel);
                var users = new ListView<String>();
                users.itemsProperty().bind(this.players);
                var label  =new Label("Players");
                var usersContainer = new VBox();
                usersContainer.getChildren().addAll(label, users);
                var buttons = new HBox();
                var leave = new Button("LEAVE");
                leave.setOnAction(actionEvent -> communicator.send("PART"));

                var changeName = new Button("CHANGE NAME");
                changeName.setOnAction(actionEvent -> namePrompt.set(true));
                namePrompt.addListener(((observable1, oldValue1, newValue1) ->{
                    if(newValue1){
                        var text = new TextField();
                        text.getStyleClass().add("textInput");
                        text.setPromptText("New Name");
                        text.setOnAction(actionEvent -> {
                            communicator.send("NICK " + text.getText());
                            namePrompt.set(false);
                        });
                        mainPane.setCenter(text);
                    }else{
                        mainPane.setCenter(channelContainer);
                    }
                }));
                buttons.getChildren().addAll(leave, changeName);
                this.isHost.addListener((obs,oValue, nValue)->{
                    if (nValue){
                        var start = new Button("START");
                        start.getStyleClass().add("menuItem");
                        start.setOnAction(actionEvent -> {
                            gameWindow.startMultiPlayerScene(this.communicator);
                        });
                        Platform.runLater(()->buttons.getChildren().add(start));
                    }else {
                        buttons.getChildren().clear();
                        buttons.getChildren().addAll(leave, changeName);
                    }
                });
                var textContainer = new VBox();
                var textbox = new TextArea();
                textbox.setEditable(false);
                textbox.setFocusTraversable(false);
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
                    sendMessage.setText("");
                });
                textContainer.getChildren().addAll(textbox, sendMessage);
                channelContainer.setLeft(textContainer);
                channelContainer.setTop(channel);
                channelContainer.setRight(usersContainer);
                channelContainer.setBottom(buttons);
                Platform.runLater(()-> {
                    mainPane.getChildren().clear();
                    mainPane.setCenter(channelContainer);
                });
            }else {
                channelTimer.cancel();
                channelTimer.purge();
                mainPane.setCenter(mainContainer);
            };
        });
        var newChannel = new Button("New Channel");
        newChannel.setOnAction(actionEvent -> this.modalShow.set(true));
        var refresh = new Button("REFRESH");
        refresh.setOnAction(actionEvent -> communicator.send("LIST"));
        var leave = new Button("LEAVE");
        leave.setOnAction(actionEvent -> Platform.runLater(gameWindow::startMenu));
        var buttons = new HBox();
        buttons.getChildren().addAll(leave, refresh, newChannel);
        mainContainer.setBottom(buttons);

    }
}
