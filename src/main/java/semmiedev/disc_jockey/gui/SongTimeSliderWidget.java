/*
 * MIT License
 *
 * Copyright (c) 2022 SemmieDev
 * Copyright (c) 2023 EnderKill98
 * Copyright (c) 2025 myueqf
 * Copyright (c) 2026 EnderPhantomWing
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
