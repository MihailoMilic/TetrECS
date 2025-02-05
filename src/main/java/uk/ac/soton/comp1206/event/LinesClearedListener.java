package uk.ac.soton.comp1206.event;
/**
 * Line Cleared Listener handles the animation after a line has been cleared.*/
public interface LinesClearedListener {
    /**
     * Plays the animation
     *
     * @param x x coordinate
     * @param y y coordinate
     * */
    public void fadeOut(int x, int y);
}
