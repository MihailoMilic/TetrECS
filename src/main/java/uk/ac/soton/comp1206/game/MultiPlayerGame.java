package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
/**MultiPlayer game*/
public class MultiPlayerGame extends Game {
    Communicator communicator;
    CountDownLatch spawnLatch;
    /**Queue for pieces.*/
    protected final Queue<GamePiece> pieces = new ConcurrentLinkedQueue<GamePiece>();
    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     * @param communicator communicator
     *
     */
    public MultiPlayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);
        logger.info("CREATING A NEW MULTIPLAYER GAME");
        this.communicator = communicator;
        this.spawnLatch = new CountDownLatch(2);
        communicator.addListener(communication -> {
            String[] message = communication.trim().split(" ");
            switch (message[0]){
                case "PIECE"->{
                    logger.info("Handling new piece");
                    pieces.add(GamePiece.createPiece(Integer.parseInt(message[1])));
                    spawnLatch.countDown();
                    logger.info("New piece created:{}", message[1]);
                    logger.info("New piece created");
                }
            }

        });
        for(int i = 0; i < 15; i++){
            communicator.send("PIECE");
        }
        Timer timerSend = new Timer();
        timerSend.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                communicator.send("PIECE");
                logger.info("Queue size:{}",pieces.size());
            }
        }, 0, 1000);
    }


    @Override
    public GamePiece spawnPiece() {
        if(pieces.size()<2){
            try{
                spawnLatch.await();
            }catch(InterruptedException e){
                logger.error(e);
            }
        }
        return pieces.remove();
    }
    @Override
    public void initialisePrimaryPieces() {
        this.currentPiece = spawnPiece();
        this.followingPiece = spawnPiece();
        nextPieceListener.nextPiece(currentPiece, followingPiece);
    }
}
