package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;
/**
 * Next Piece listener supplies the UI with the next two pieces it can display for the player.*/
public interface NextPieceListener {
    /**
     * Sets the piece and following piece onto their designated boards.
     *
     * @param piece peace to be played next.
     * @param followingPiece peace that is next in the queue after piece.
     * */
    public void nextPiece(GamePiece piece, GamePiece followingPiece);
}
