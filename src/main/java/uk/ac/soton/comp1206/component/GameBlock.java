package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private final BooleanProperty isHighlighted = new SimpleBooleanProperty(false);
    private static final Logger logger = LogManager.getLogger(GameBlock.class);
    private AnimationTimer flashAndFadeAnimation;

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT.saturate(),
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    private boolean clearAnimationInProgress = false;
    private long lastAnimationTime = 0;
    private final long animationDuration = 1000000000L;

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
        isHighlighted.addListener(this::updateValue);

    }


    private void updateValue(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean1) {
        paint();
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }
    /**
     * Handles highlighting a block
     *
     * @param  bool highlight/unhighlight this block
     * */
    public void setHighlighted(boolean bool){
        isHighlighted.set(bool);
    }
    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {

                paintEmpty();

        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
    }
    /**
     * Manages the clear animation after a line has been filled.*/
    public void fadeOut() {
        clearAnimationInProgress = true;
        lastAnimationTime = System.nanoTime();

        // Start AnimationTimer
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsedTime = now - lastAnimationTime;
                double progress = (double) elapsedTime / animationDuration;
                if (progress < 1.0) {
                    // Animation finished
                    // Gradually decrease opacity
                    double opacity = 1 - progress;

                    // Clear canvas
                    GraphicsContext gc = getGraphicsContext2D();
                    gc.clearRect(0, 0, width, height);

                    // Fill with semi-transparent color
                    Color transparentColor = new Color(1, 1, 1, opacity);
                    gc.setFill(transparentColor);
                    gc.fillRect(0, 0, width, height);

                    // Border
                    if (isHighlighted.get()) {
                        gc.setLineWidth(5);
                        gc.setStroke(transparentColor.invert());
                    } else {
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(1);
                    }
                    gc.strokeRect(0, 0, width, height);
                }else{
                    clearAnimationInProgress = false;
                    stop(); // Stop AnimationTimer
                    paintEmpty();
                }
            }
        }.start();
    }
    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.WHITE);
        gc.fillRect(0,0, width, height);

        //Border
        if(isHighlighted.get()) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(5);
        }else{
            gc.setStroke(Color.PURPLE);
            gc.setLineWidth(1);
        }
        gc.strokeRect(0,0,width,height);
    }


    /**
     * Paint this canvas with the given colour
     *
     * @param colour the colour to paint
     */
    private void paintColor(Color colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        LinearGradient gradient = new LinearGradient(0, 0, width, height, false, CycleMethod.NO_CYCLE,
                new Stop(0, colour.brighter().brighter()),
                new Stop(.5, colour),
                new Stop(1, colour.darker().darker()
                )
        );

        // Fill with gradient
        gc.setFill(gradient);
        gc.fillRect(0, 0, width, height);

        //Border
        if(isHighlighted.get()){
            gc.setLineWidth(5);
            gc.setStroke(colour.invert());
        }else{
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
        }
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

}
