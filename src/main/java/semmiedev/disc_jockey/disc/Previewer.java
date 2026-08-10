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

package semmiedev.disc_jockey.disc;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import semmiedev.disc_jockey.DiscJockey;

import java.io.IOException;

//#if MC < 26.1
public class Previewer implements ClientTickEvents.StartWorldTick {
//#else
//$$ public class Previewer implements ClientTickEvents.StartLevelTick {
//#endif
    public boolean running;

    private int i;
    private float tick;
    private Song song;

    public void start(Song song) {
        try {
            SongLoader.ensureSongLoaded(song);
        } catch (IOException e) {
            DiscJockey.LOGGER.error("Failed to load song data for preview {}", song.fileName, e);
            return;
        }
        this.song = song;
        DiscJockey.TICK_LISTENERS.add(this);
        running = true;
    }

    public void stop() {
        Minecraft.getInstance().schedule(() -> DiscJockey.TICK_LISTENERS.remove(this));
        running = false;
        i = 0;
        tick = 0;
    }

    @Override
    public void onStartTick(@NonNull ClientLevel world) {
        while (running) {
            long note = song.notes[i];
            if ((short)note == Math.round(tick)) {
                Vec3 pos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
                world.playLocalSound(pos.x, pos.y, pos.z, Note.INSTRUMENTS[(byte)(note >> Note.INSTRUMENT_SHIFT)].getSoundEvent().value(), SoundSource.RECORDS, 3, (float)Math.pow(2.0, ((byte)(note >> Note.NOTE_SHIFT) - 12) / 12.0), false);
                i++;
                if (i >= song.notes.length) {
                    stop();
                    break;
                }
            } else {
                break;
            }
        }

        tick += song.tempo / 100f / 20f;
    }
}
