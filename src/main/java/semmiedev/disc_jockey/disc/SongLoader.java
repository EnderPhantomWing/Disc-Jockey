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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import semmiedev.disc_jockey.DiscJockey;
import semmiedev.disc_jockey.gui.SongListWidget;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class SongLoader {
    public static final ArrayList<Song> SONGS = new ArrayList<>();
    public static final ArrayList<String> SONG_SUGGESTIONS = new ArrayList<>();
    public static volatile boolean loadingSongs;
    public static volatile boolean showToast;

    public static void loadSongs() {
        if (loadingSongs) return;
        new Thread(() -> {
            loadingSongs = true;
            SONGS.clear();
            SONG_SUGGESTIONS.clear();
            SONG_SUGGESTIONS.add("Songs are loading, please wait");
            for (File file : DiscJockey.songsFolder.listFiles()) {
                Song song = null;
                try {
                    song = loadSong(file);
                } catch (Exception exception) {
                    DiscJockey.LOGGER.error("Unable to read or parse song {}", file.getName(), exception);
                }
                if (song != null) SONGS.add(song);
            }
            for (Song song : SONGS) SONG_SUGGESTIONS.add(song.displayName);
            DiscJockey.config.favorites.removeIf(favorite -> SongLoader.SONGS.stream().map(song -> song.fileName).noneMatch(favorite::equals));

            if (showToast && Minecraft.getInstance().font != null) SystemToast.add(Minecraft.getInstance().getToastManager(), SystemToast.SystemToastId.PACK_LOAD_FAILURE, DiscJockey.NAME, Component.translatable(DiscJockey.MOD_ID+".loading_done"));
            showToast = true;
            loadingSongs = false;
        }).start();
    }

    public static Song loadSong(File file) throws IOException {
        if (file.isFile()) {
            BinaryReader reader = new BinaryReader(Files.newInputStream(file.toPath()));
            Song song = new Song();

            song.fileName = file.getName().replaceAll("[\\n\\r]", "");

            song.length = reader.readShort();

            boolean newFormat = song.length == 0;
            if (newFormat) {
                song.formatVersion = reader.readByte();
                song.vanillaInstrumentCount = reader.readByte();
                song.length = reader.readShort();
            }

            song.height = reader.readShort();
            song.name = reader.readString().replaceAll("[\\n\\r]", "");
            song.author = reader.readString().replaceAll("[\\n\\r]", "");
            song.originalAuthor = reader.readString().replaceAll("[\\n\\r]", "");
            song.description = reader.readString().replaceAll("[\\n\\r]", "");
            song.tempo = reader.readShort();
            song.autoSaving = reader.readByte();
            song.autoSavingDuration = reader.readByte();
            song.timeSignature = reader.readByte();
            song.minutesSpent = reader.readInt();
            song.leftClicks = reader.readInt();
            song.rightClicks = reader.readInt();
            song.blocksAdded = reader.readInt();
            song.blocksRemoved = reader.readInt();
            song.importFileName = reader.readString().replaceAll("[\\n\\r]", "");

            if (newFormat) {
                song.loop = reader.readByte();
                song.maxLoopCount = reader.readByte();
                song.loopStartTick = reader.readShort();
            }

            song.displayName = song.name.replaceAll("\\s", "").isEmpty() ? song.fileName : song.name+" ("+song.fileName+")";
            song.entry = new SongListWidget.SongEntry(song, SONGS.size());
            song.entry.favorite = DiscJockey.config.favorites.contains(song.fileName);
            song.searchableFileName = song.fileName.toLowerCase().replaceAll("\\s", "");
            song.searchableName = song.name.toLowerCase().replaceAll("\\s", "");

            short tick = -1;
            short jumps;
            while ((jumps = reader.readShort()) != 0) {
                tick += jumps;
                short layer = -1;
                while ((jumps = reader.readShort()) != 0) {
                    layer += jumps;

                    byte instrumentId = reader.readByte();
                    byte noteId = (byte)(reader.readByte() - 33);

                    if (newFormat) {
                        // Data that is not needed as it only works with commands
                        reader.readByte(); // Velocity
                        reader.readByte(); // Panning
                        reader.readShort(); // Pitch
                    }

                    if (noteId < 0) {
                        noteId = 0;
                    } else if (noteId > 24) {
                        noteId = 24;
                    }

                    Note note = new Note(Note.INSTRUMENTS[instrumentId], noteId);
                    if (!song.uniqueNotes.contains(note)) song.uniqueNotes.add(note);

                    song.notes = Arrays.copyOf(song.notes, song.notes.length + 1);
                    song.notes[song.notes.length - 1] = tick | layer << Note.LAYER_SHIFT | (long)instrumentId << Note.INSTRUMENT_SHIFT | (long)noteId << Note.NOTE_SHIFT;
                }
            }

            return song;
        }
        return null;
    }

    public static void sort() {
        SONGS.sort(Comparator.comparing(song -> song.displayName));
    }
}
