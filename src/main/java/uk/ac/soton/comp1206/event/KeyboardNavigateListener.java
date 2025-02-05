package uk.ac.soton.comp1206.event;
/**
 * Keyboard Navigate Listener allows user to use keyboard to play the game. It handles rotating, going up,down,left,right, pressing. */
public interface KeyboardNavigateListener {
    /**
     * Handles going up.*/
    public void up();
    /**
     * Handles going down*/
    public void down();
    /**
     * Handles going left*/
    public void left();
    /**
     * Handles going right*/
    public void right();
    /**
     * Handles pressing on a block*/
    public void press();
    /**
     * Returns coordinates
     * @return int[] coordinates of currently used block
     * */
    public int[] coordinate();
}
