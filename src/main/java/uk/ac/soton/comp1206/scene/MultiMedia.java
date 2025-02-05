package uk.ac.soton.comp1206.scene;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URL;

import static javafx.scene.media.MediaPlayer.INDEFINITE;

/**
 * This class manages multimedia playback, including audio and music.
 */
public class MultiMedia {
    /** The player for audio files. */
    private final MediaPlayer audioPlayer;

    /** The player for music files. */
    private final MediaPlayer musicPlayer;

    /**
     * Constructs a MultiMedia object with the given media file.
     *
     * @param media the path to the media file
     */
    public MultiMedia(String media) {
        String uriString = new File(media).toURI().toString();
        audioPlayer = new MediaPlayer(new Media(uriString));
        musicPlayer = new MediaPlayer(new Media(uriString));
    }

    /**
     * Starts playing the music in a loop indefinitely.
     */
    public void playMusic() {
        musicPlayer.setCycleCount(INDEFINITE);
        musicPlayer.isAutoPlay();
        musicPlayer.play();
    }

    /**
     * Stops the music playback.
     */
    public void stopMusic() {
        musicPlayer.stop();
    }

    /**
     * Starts playing the audio file once.
     */
    public void playAudio() {
        audioPlayer.setCycleCount(1);
        audioPlayer.play();
    }

    /**
     * Stops the audio playback.
     */
    public void stopAudio() {
        audioPlayer.stop();
    }

    /**
     * Sets the volume level for both audio and music playback.
     *
     * @param volume the volume level (a value between 0.0 and 1.0)
     */
    public void setVolume(double volume) {
        if (volume <= 1 && volume >= 0) {
            audioPlayer.setVolume(volume);
            musicPlayer.setVolume(volume);
        }
    }
}
