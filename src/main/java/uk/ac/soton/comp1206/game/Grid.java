package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;

import java.util.Objects;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {
    private static final Logger logger = LogManager.getLogger(Grid.class);

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Handles the checking of whether a piece can be placed at these coordinates.
     * @param x x coordinate
     * @param y y coordinate
     * @param piece Game Piece to be checked
     * @return boolean if piece can be played
     * */
    public boolean canPlayPeace(GamePiece piece, int x, int y){
        int pressed = getGridProperty(x,y).get();

        int[][] pieceGrid = piece.getBlocks();

        if (pressed ==0){
            for (int i=0; i<pieceGrid.length; i++){
                for (int j=0; j<pieceGrid[i].length; j++){
                    int number = pieceGrid[i][j];

                    if (number!=0){
                        int offsetX = x+i-1;
                        int offsetY = y+j-1;
                        if (offsetY <0 || offsetX<0 || offsetX>=cols || offsetY>=rows){
                            return false;
                        }else{
                            int gridValue = getGridProperty(offsetX, offsetY).get();
                            if(gridValue !=0) return false;
                        }

                    }
                }
            }
            return true;
        }else return false;
    }
    /**
     * Replaces the value in the grid of the block with the value of the piece played
     *
     * @param piece piece to be played
     * @param x x-coordinate
     * @param y y-coordinate
     * @return boolean if piece can be played
     * */
    public boolean playPiece(GamePiece piece, int x, int y){

        int[][] pieceGrid = piece.getBlocks();
        if(canPlayPeace(piece,x,y)){
            for (int i=0; i<pieceGrid.length; i++){
                for (int j=0; j<pieceGrid[i].length; j++){
                    int number = pieceGrid[i][j];

                    int offsetX = x+i-1;
                    int offsetY = y+j-1;

                    if(number!=0){
                        set(offsetX,offsetY, piece.getValue());
                    }
                }
            }
            return true;
        }else {
            logger.info("Block Populated");
            return false;
        }
    }

}
