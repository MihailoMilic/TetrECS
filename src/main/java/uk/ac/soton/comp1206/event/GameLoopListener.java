package uk.ac.soton.comp1206.event;
/**
 * Game Loop Listener listens for starting, and ending of each life, and therefore the game as well. It is used to present the backend timer onto the UI*/
public interface GameLoopListener {

/**
 * Starts the timer*/
    public void start();
    /**
     * Stops the timer*/
    public void stop();
    /**
     * Handles end of the game*/
    public void endGame();
    /**
     * Sends the player to the scores scene.*/
    public void showScores();
}
