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

package semmiedev.disc_jockey.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import semmiedev.disc_jockey.DiscJockey;

import java.util.ArrayList;

@me.shedaniel.autoconfig.annotation.Config(name = DiscJockey.MOD_ID)
@me.shedaniel.autoconfig.annotation.Config.Gui.Background("textures/block/note_block.png")
public class Config implements ConfigData {
    public boolean hideWarning;
    @ConfigEntry.Gui.Tooltip(count = 2) public boolean disableAsyncPlayback;
    @ConfigEntry.Gui.Tooltip(count = 2) public boolean omnidirectionalNoteBlockSounds = true;

    public enum ExpectedServerVersion {
        All,
        v1_20_4_Or_Earlier,
        v1_20_5_Or_Later;

        @Override
        public String toString() {
            return switch (this) {
                case All -> "All (universal)";
                case v1_20_4_Or_Earlier -> "≤1.20.4";
                case v1_20_5_Or_Later -> "≥1.20.5";
            };
        }
    }

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip(count = 4)
    public ExpectedServerVersion expectedServerVersion = ExpectedServerVersion.All;

    public enum TuningSpeed {
        Snail,
        Safe,
        Spigot,
        Flash;

        @Override
        public String toString() {
            return switch(this) {
                case Snail -> "Snail (10/sec)";
                case Safe -> "Safe (20/sec)";
                case Spigot -> "Spigot (recommended)";
                case Flash -> "Flash";
            };
        }
    }

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip(count = 7)
    public TuningSpeed tuningSpeed = TuningSpeed.Spigot;

    public enum PlaybackPacketRatelimit {
        Limit100,
        Limit200,
        Limit300,
        Limit500,
        NoLimit;

        @Override
        public String toString() {
            return switch(this) {
                case Limit100 -> "100 Packets/sec";
                case Limit200 -> "200 Packets/sec";
                case Limit300 -> "300 Packets/sec";
                case Limit500 -> "500 Packets/sec";
                case NoLimit -> "No Limit";
            };
        }

        public int getReducePacketsPer100Millis() {
            return switch(this) {
                case Limit100 -> 30 / 10;
                case Limit200 -> 130 / 10;
                case Limit300 -> 200 / 10;
                case Limit500 -> 300 / 10;
                case NoLimit -> Integer.MAX_VALUE;
            };
        }

        public int getMaxPacketsPer100Millis() {
            return switch(this) {
                case Limit100 -> 70 / 10;
                case Limit200 -> 150 / 10;
                case Limit300 -> 250 / 10;
                case Limit500 -> 450 / 10;
                case NoLimit -> Integer.MAX_VALUE;
            };
        }

    }

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip(count = 4)
    public PlaybackPacketRatelimit playbackPacketRatelimit = PlaybackPacketRatelimit.Limit500;


    @ConfigEntry.Gui.Tooltip(count = 1)
    public float delayPlaybackStartBySecs = 0.0f;

    @ConfigEntry.Gui.Tooltip(count = 3) public boolean instrumentDetectionWorkaround = true;

    @ConfigEntry.Gui.Excluded
    public ArrayList<String> favorites = new ArrayList<>();

}
