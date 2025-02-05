package uk.ac.soton.comp1206.game;

import javafx.beans.property.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.KeyboardNavigateListener;
import uk.ac.soton.comp1206.event.LinesClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Game class handles the main logic, state, and properties of the TetrECS game.
 * Methods to manipulate the game state and to handle actions made by the player should take place inside this class.
 */
public class Game implements KeyboardNavigateListener {
    /**Logger*/
    protected static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     **/
    protected final Grid grid;
    /**
     * Next piece grid*/
    protected final Grid nextPieceGrid;
    /**
     * Following piece grid*/
    protected final Grid followingPieceGrid;
    /**
     * Score*/
    protected final IntegerProperty score;
    /**
     * Level*/
    protected final IntegerProperty level;
    /**
     * Lives*/
    protected final IntegerProperty lives;
    /**
     * Multiplier*/
    protected final IntegerProperty multiplier;
    GamePiece currentPiece;

    GamePiece followingPiece;
    /**
     * Next piece listener.*/
    protected NextPieceListener nextPieceListener;
    /**
     * Lines Cleared Listener*/
    private LinesClearedListener linesClearedListener;

    private int marker;

    private boolean swapBool;
    int[] coordinate;
    /**
     * Timer*/
    protected Timer timer;
    /**
     * Timer's task*/
    protected TimerTask task;
    private GameLoopListener gameLoopListener;
    /**
     * Time Delay before life ends.*/
    protected final LongProperty timeDelay = new SimpleLongProperty(12000);

    /**
     * Start the timer.
     */
    private void startTimer() {
        if (gameLoopListener != null) {
            gameLoopListener.start();
        }
    }

    /**
     * Stop the timer.
     */
    private void stopTimer() {
        if (gameLoopListener != null) {
            gameLoopListener.stop();
        }
    }

    /**
     * Reschedule the timer.
     */
    public void reschedule() {
        stopTimer();
        // Cancel the existing timer if it exists
        if (timer != null) {
            timer.cancel();
            timer.purge(); // Ensure all canceled tasks are removed from the timer's task queue
        }

        // Create a new timer and task
        timer = new Timer("Timer");
        task = new TimerTask() {
            @Override
            public void run() {
                stopTimer();
                if (lives.get() > 0) {
                    logger.info(new Date().getTime());
                    logger.info("you lost a life");
                    nextPiece();
                    lives.set(lives.get() - 1);
                } else {
                    timer.cancel();
                    timer.purge();
                    gameLoopListener.endGame();
                    gameLoopListener.showScores();
                    logger.info("Game ended");

                }
                logger.info("Time delay:{}", timeDelay.get());
                startTimer();
            }
        };
        // Schedule the new task with the new timer
        timer.scheduleAtFixedRate(task, timeDelay.get(), timeDelay.get());
        startTimer();
    }

    /**
     * Set the coordinate.
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void setCoordinate(int x, int y) {
        this.coordinate = new int[]{x, y};
    }

    /**
     * Set the lines cleared listener.
     * @param listener the lines cleared listener
     */
    public void setLinesClearedListener(LinesClearedListener listener) {
        this.linesClearedListener = listener;
    }

    /**
     * Set the game loop listener.
     * @param listener the game loop listener
     */
    public void setGameLoopListener(GameLoopListener listener) {
        this.gameLoopListener = listener;
    }

    /**
     * Get the following piece grid.
     * @return the following piece grid
     */
    public Grid getFollowingPieceGrid() {
        return followingPieceGrid;
    }

    /**
     * End of game functionality.
     */
    void endOfGameFunctionality() {}

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        //Create a new grid model to represent the game state
        this.grid = new Grid(cols, rows);
        this.coordinate = new int[]{2, 2};
        this.nextPieceGrid = new Grid(3, 3);
        this.followingPieceGrid = new Grid(3, 3);
        //Create a new piece
        this.score = new SimpleIntegerProperty(0);

        this.level = new SimpleIntegerProperty(0);
        this.lives = new SimpleIntegerProperty(3);
        this.multiplier = new SimpleIntegerProperty(1);
        this.swapBool = false;
        this.timer = new Timer("Timer");
        setUpListeners();

        this.task = new TimerTask() {
            @Override
            public void run() {
                stopTimer();
                if (lives.get() > 0) {
                    logger.info(new Date().getTime());
                    logger.info("you lost a life");
                    nextPiece();
                    lives.set(lives.get() - 1);
                } else {
                    timer.cancel();
                    timer.purge();
                    gameLoopListener.endGame();
                    gameLoopListener.showScores();
                    logger.info("Game ended");
                }
                logger.info("Time delay:{}", timeDelay.get());
                startTimer();
            }
        };
        this.timer.scheduleAtFixedRate(task, timeDelay.get(), timeDelay.get());
        startTimer();
    }

    /**
     * Initialise primary pieces.
     */
    public void initialisePrimaryPieces() {
        this.currentPiece = spawnPiece();
        this.followingPiece = spawnPiece();
        nextPieceListener.nextPiece(currentPiece, followingPiece);
    }

    private void setUpListeners() {
        level.addListener(((observableValue, oldValue, newValue) -> {
            timeDelay.set(Math.max(2500, 12000 - 500 * newValue.longValue()));
            logger.info("New time delay:{}", timeDelay.get());
        }));
        timeDelay.addListener((observableValue, oldValue, newValue) -> {
            reschedule();
            logger.info("new time delay. New timer");
        });
    }

    /**
     * Get the time delay property.
     * @return the time delay property
     */
    public LongProperty getTimeDelay() {
        return timeDelay;
    }

    /**
     * Start the game.
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start.
     */
    public void initialiseGame() {
        logger.info("Initialising game");
    }

    /**
     * Handle what should happen when a particular block is clicked.
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        //play the new piece
        if (grid.playPiece(currentPiece, x, y)) {
            reschedule();
            afterPiece();

            if (swapBool || marker > 0) {
                logger.info("swapped: {}", swapBool);
                if (marker == 0) {
                    logger.info("Increased marker by one.");
                    marker++;
                    swapBool = false;
                } else {
                    marker = 0;
                    logger.info("Resetting the marker");
                }
            }
            logger.info("marker:" + marker);
            //get the next one
            nextPiece();
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board.
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the current piece.
     * @return the current piece
     */
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    /**
     * Get the number of columns in this game.
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game.
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the next piece grid.
     * @return the next piece grid
     */
    public Grid getNextPieceGrid() {
        return nextPieceGrid;
    }

    /**
     * Get the score property.
     * @return the score property
     */
    public IntegerProperty getScore() {
        return this.score;
    }

    /**
     * Get the level property.
     * @return the level property
     */
    public IntegerProperty getLevel() {
        return this.level;
    }

    /**
     * Get the lives property.
     * @return the lives property
     */
    public IntegerProperty getLives() {
        return this.lives;
    }

    /**
     * Get the multiplier property.
     * @return the multiplier property
     */
    public IntegerProperty getMultiplier() {
        return this.multiplier;
    }

    /**
     * Get the following piece.
     * @return the following piece
     */
    public GamePiece getFollowingPiece() {
        return followingPiece;
    }

    /**
     * Set the next piece listener.
     * @param listener the next piece listener
     */
    public void setNextPieceListener(NextPieceListener listener) {
        this.nextPieceListener = listener;
    }

    /**
     * Creates a new random piece.
     * @return spawned game piece
     */
    public GamePiece spawnPiece() {
        Random random = new Random();
        return GamePiece.createPiece(random.nextInt(15));
    }

    /**
     * Move to the next piece.
     */
    public void nextPiece() {
        this.currentPiece = followingPiece;
        followingPiece = spawnPiece();
        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        }
        logger.info("Following piece created: {}", followingPiece.getValue());
    }

    /**
     * Swap the current piece.
     */
    public void swapCurrentPiece() {
        if (marker == 0) {
            swapBool = !swapBool;
            logger.info("Pieces swapped");
            GamePiece f = followingPiece;
            followingPiece = currentPiece;
            currentPiece = f;
        } else logger.error("The swapped block must be played");
    }

    /**
     * Scan for full rows and mark in the array the index of the row which is full with true.
     * @return an array of booleans with the size equal to the number of rows
     */
    public boolean[] scanForFullRows() {
        boolean[] fullRows = new boolean[rows];
        for (int i = 0; i < rows; i++) {
            int numFilled = 0;
            for (int j = 0; j < cols; j++) {
                if (grid.get(i, j) != 0) {
                    numFilled++;
                }
            }
            fullRows[i] = numFilled == cols;
        }
        return fullRows;
    }

    /**
     * Scan for full columns and mark in the array the index of the column which is full with true.
     * @return an array of booleans with the size equal to the number of columns
     */
    public boolean[] scanForFullCols() {
        boolean[] fullCols = new boolean[cols];
        for (int j = 0; j < cols; j++) {
            int numFilled = 0;
            for (int i = 0; i < rows; i++) {
                if (grid.get(i, j) != 0) {
                    numFilled++;
                }
            }
            fullCols[j] = numFilled == rows;
        }
        return fullCols;
    }

    /**
     * Count the number of true values in an array.
     * @param array the boolean array
     * @return the number of true values
     */
    public int numOfFull(boolean[] array) {
        int num = 0;
        for (boolean bool : array) {
            if (bool) {
                num++;
            }
        }
        return num;
    }

    /**
     * Calculates the score gained after the placing of a block.
     * @param lines int number of lines cleared
     * @param blocks int number of distinct blocks cleared
     * @return int total score
     */
    public int scoreCalc(int lines, int blocks) {
        return lines * blocks * 10 * multiplier.get();
    }

    /**
     * Perform actions after placing a piece.
     */
    public void afterPiece() {
        boolean[] fullCols = scanForFullCols();
        boolean[] fullRows = scanForFullRows();

        int fullLines = numOfFull(fullCols) + numOfFull(fullRows);
        int rowColIntersection = numOfFull(fullCols) * numOfFull(fullRows);
        int blocksCleared = numOfFull(fullCols) * rows + numOfFull(fullRows) * cols - rowColIntersection;

        logger.info("Blocks cleared: " + blocksCleared);
        score.set(score.get() + scoreCalc(fullLines, blocksCleared));

        logger.info("New score: " + score.get());

        if (fullLines > 0) {
            logger.info("Number of lines cleared: " + fullLines);
            multiplier.set(multiplier.get() + 1);
        } else {
            multiplier.set(1);
        }

        int newLevel = score.get() / 1000;
        if (newLevel >= 1) {
            logger.info("New level:{}", level.get());
            level.set((int) (double) newLevel);
        }

        for (int i = 0; i < rows; i++) {
            if (fullRows[i]) {
                for (int j = 0; j < cols; j++) {
                    if (grid.get(i, j) != 0) {
                        linesClearedListener.fadeOut(i, j);
                        grid.set(i, j, 0);
                    }
                }
            }
        }
        for (int j = 0; j < cols; j++) {
            if (fullCols[j]) {
                for (int i = 0; i < rows; i++) {
                    if (grid.get(i, j) != 0) {
                        linesClearedListener.fadeOut(i, j);
                        grid.set(i, j, 0);
                    }
                }
            }
        }

    }

    /**
     * Rotate the current piece.
     */
    public void rotatePiece() {
        currentPiece.rotate();
    }

    /**
     * Rotate the current piece by a given number of rotations.
     * @param numRotation the number of rotations
     */
    public void rotatePiece(int numRotation) {
        currentPiece.rotate(numRotation);
    }

    @Override
    public void up() {
        int y = coordinate[1];
        if (y > 0) {
            coordinate[1] = coordinate[1] - 1;
        } else {
            logger.error("Cannot go any more up");
        }
    }

    @Override
    public void down() {
        int y = coordinate[1];
        if (y < rows - 1) {
            coordinate[1] = y + 1;
        } else {
            logger.error("Cannot go any more down");
        }
    }

    @Override
    public void left() {
        int x = coordinate[0];
        if (x > 0) {
            coordinate[0] = x - 1;
        } else {
            logger.error("Cannot go anymore left");
        }
    }

    @Override
    public void right() {
        int x = coordinate[0];
        if (x < cols - 1) {
            coordinate[0] = x + 1;
        } else {
            logger.error("Cannot go anymore right.");
        }
    }

    @Override
    public void press() {
        if (grid.playPiece(currentPiece, coordinate[0], coordinate[1])) {
            reschedule();
            afterPiece();
            //get the next one
            if (swapBool || marker > 0) {
                logger.info("swapped: {}", swapBool);
                if (marker == 0) {
                    logger.info("Increased marker by one.");
                    marker++;
                    swapBool = false;
                } else {
                    marker = 0;
                    logger.info("Resetting the marker");
                }
            }
            logger.info("marker:" + marker);

            nextPiece();
        }
    }

    /**
     * Stop the game.
     */
    public void stopGame() {
        timer.cancel();
        timer.purge();
    }

    @Override
    public int[] coordinate() {
        return coordinate;
    }
}
