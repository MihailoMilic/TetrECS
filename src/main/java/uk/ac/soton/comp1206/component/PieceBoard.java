package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
/**Piece Board, board for pieces.*/
public class PieceBoard extends GameBoard{
    /**Makes Piece Board
     *
     *
     * @param grid Grid to use
     * @param height set hight
     * @param width set width*/
    public PieceBoard(Grid grid, double width, double height){
        super(grid, width, height);
        super.toggleForbid(true);
    }
    /**
     * Displays piece onto this board
     *
     * @param piece a game piece wanting to be displayed
     * */
    public void display(GamePiece piece){
        int[][] pieceGrid = piece.getBlocks();
        for (int i=0; i<3; i++){
            for (int j =0; j<3; j++){
                grid.set(i,j, pieceGrid[i][j]);
            }
        }
    }

}
