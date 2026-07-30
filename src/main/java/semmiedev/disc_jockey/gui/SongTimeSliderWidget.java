package semmiedev.disc_jockey.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import semmiedev.disc_jockey.DiscJockey;

public class SongTimeSliderWidget extends AbstractSliderButton {

    public SongTimeSliderWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), 0);
    }

    private static String padZeroes(int number, int length) {
        StringBuilder builder = new StringBuilder("" + number);
        while(builder.length() < length)
            builder.insert(0, '0');
        return builder.toString();
    }

    private static String formatTimestamp(int seconds) {
        return padZeroes(seconds / 60, 2) + ":" + padZeroes(seconds % 60, 2);
    }

    @Override
    protected void updateMessage() {
        if(DiscJockey.SONG_PLAYER.song == null)
            setMessage(Component.empty());
        else
            setMessage(Component.literal(formatTimestamp((int) DiscJockey.SONG_PLAYER.getSongElapsedSeconds()) + " / " + formatTimestamp((int) DiscJockey.SONG_PLAYER.song.getLengthInSeconds())));
    }

    @Override
    protected void applyValue() {
        if(DiscJockey.SONG_PLAYER.song == null) return;
        double total = DiscJockey.SONG_PLAYER.song.getLengthInSeconds();
        double seconds = value * total;
        DiscJockey.SONG_PLAYER.setSongElapsedSeconds(seconds);
    }

    public void update() {
        if(DiscJockey.SONG_PLAYER.song == null) return;
        double elapsed = DiscJockey.SONG_PLAYER.getSongElapsedSeconds();
        double total = DiscJockey.SONG_PLAYER.song == null ? 1 : DiscJockey.SONG_PLAYER.song.getLengthInSeconds();
        value = elapsed / total;
        updateMessage();
    }
}
