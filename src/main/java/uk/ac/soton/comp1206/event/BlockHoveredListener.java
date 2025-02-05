package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;

/**
 * The Block Hovered listener is used to handle the event when a block in a GameBoard is hovered. It passes the
 * GameBlock that was clicked in the message
 */
public interface BlockHoveredListener {
    /**
     * Handle a block clicked event
     * @param block the block that was hovered
     * @param board Board from which this block comes
     */
    public void hover(GameBlock block, GameBoard board);
}
