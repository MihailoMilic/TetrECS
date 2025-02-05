package uk.ac.soton.comp1206.network;

import com.neovisionaries.ws.client.*;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Uses web sockets to talk to a web socket server and relays communication to attached listeners
 *
 * YOU DO NOT NEED TO WORRY ABOUT THIS CLASS! Leave it be :-)
 */
public class Communicator {

    private static final Logger logger = LogManager.getLogger(Communicator.class);

    /**
     * Attached communication listeners listening to messages on this Communicator. Each will be sent any messages.
     */
    private final List<CommunicationsListener> handlers = new ArrayList<>();

    private WebSocket ws = null;
    private Runnable onConnectedCallback;

    /**
     * Create a new communicator to the given web socket server
     *
     * @param server server to connect to
     */

    public Communicator(String server ) {
        try {
            var socketFactory = new WebSocketFactory();

            //Connect to the server
            ws = socketFactory.createSocket(server);
            ws.connect();
            logger.info("Connected to " + server);

            //When a message is received, call the receive method
            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    Communicator.this.receive(websocket, message);
                }
                @Override
                public void onPingFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
                    logger.info("Ping? Pong!");
                }
            });

            //Error handling
            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    if(message.startsWith("ERROR")) {
                        logger.error(message);
                    }
                }
                @Override
                public void handleCallbackError(WebSocket webSocket, Throwable throwable) throws Exception {
                    logger.error("Callback Error:" + throwable.getMessage());
                    throwable.printStackTrace();
                }
                @Override
                public void onError(WebSocket webSocket, WebSocketException e) throws Exception {
                    logger.error("Error:" + e.getMessage());
                    e.printStackTrace();
                }
            });
            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    logger.info("Connected to " + server);
                    if(onConnectedCallback != null) {
                        onConnectedCallback.run();
                    }
                }
            });

        } catch (Exception e){
            logger.error("Socket error: " + e.getMessage());
            e.printStackTrace();

            Alert error = new Alert(Alert.AlertType.ERROR,"Unable to communicate with the TetrECS server\n\n" + e.getMessage() + "\n\nPlease ensure you are connected to the VPN");
            error.showAndWait();
            System.exit(1);
        }
    }
/**
 * Sends a message to the server
 * @param onConnectedCallback on Connected Call Back
 * */
public void setOnConnectedCallback(Runnable onConnectedCallback) {
    this.onConnectedCallback = onConnectedCallback;
}
    /** Send a message to the server
     *
     * @param message Message to send
     */
    public void send(String message) {
        logger.info("Sending message: " + message);

        ws.sendText(message);
    }

    /**
     * Add a new listener to receive messages from the server
     * @param listener the listener to add
     */
    public void addListener(CommunicationsListener listener) {
        logger.info("Listener added");
        this.handlers.add(listener);
    }

    /**
     * Clear all current listeners
     */
    public void clearListeners() {
        logger.info("Cleared listeners");
        this.handlers.clear();
    }

    /** Receive a message from the server. Relay to any attached listeners
     *
     * @param websocket the socket
     * @param message the message that was received
     */
    private void receive(WebSocket websocket, String message) {
        if (handlers.isEmpty()) logger.warn("No handlers ");
        logger.info("Received message:{} ", message);
        for(CommunicationsListener handler : handlers) {
            logger.info("Handling " + message);
            handler.receiveCommunication(message);
        }
    }

}
