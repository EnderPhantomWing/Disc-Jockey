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
import java.util.*;

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
            List<Song> loadedSongs = Arrays.stream(Objects.requireNonNull(DiscJockey.songsFolder.listFiles()))
                    .parallel()
                    .filter(file -> {
                        String fileName = file.getName().toLowerCase();
                        // Skip MIDI files if experimental MIDI features are disabled
                        return !((fileName.endsWith(".mid") || fileName.endsWith(".midi")) && !DiscJockey.config.enableExperimentalMIDI);
                    })
                    .map(file -> {
                        try {
                            return loadSong(file, false);
                        } catch (Exception exception) {
                            DiscJockey.LOGGER.error("Unable to read or parse song {}", file.getName(), exception);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
            SONGS.addAll(loadedSongs);
            for (Song song : SONGS) SONG_SUGGESTIONS.add(song.displayName);
            DiscJockey.config.favorites.removeIf(favorite -> SongLoader.SONGS.stream().map(song -> song.fileName).noneMatch(favorite::equals));

            if (showToast) SystemToast.add(Minecraft.getInstance().getToastManager(), SystemToast.SystemToastId.PACK_LOAD_FAILURE, DiscJockey.NAME, Component.translatable(DiscJockey.MOD_ID+".loading_done"));
            showToast = true;
            loadingSongs = false;
        }).start();
    }

    public static Song loadSong(File file) throws IOException {
        return loadSong(file, true);
    }

    public static Song loadSong(File file, boolean loadNotes) throws IOException {
        if (file.isFile()) {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".mid") || fileName.endsWith(".midi")) {
                if (!DiscJockey.config.enableExperimentalMIDI) {
                    return null;
                }
                try {
                    Song song = MidiLoader.loadFromMidi(file);
                    song.displayName = song.name.replaceAll("\\s", "").isEmpty() ? song.fileName : song.name+" ("+song.fileName+")";
                    song.entry = new SongListWidget.SongEntry(song, SONGS.size());
                    song.entry.favorite = DiscJockey.config.favorites.contains(song.fileName);
                    song.searchableFileName = song.fileName.toLowerCase().replaceAll("\\s", "");
                    song.searchableName = song.name.toLowerCase().replaceAll("\\s", "");
                    return song;
                } catch (Exception e) {
                    throw new IOException("Failed to load MIDI file", e);
                }
            }
            try (java.io.InputStream inputStream = Files.newInputStream(file.toPath())) {
                BinaryReader reader = new BinaryReader(inputStream);
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

                song.displayName = song.name.replaceAll("\\s", "").isEmpty() ? song.fileName : song.name + " (" + song.fileName + ")";
                song.entry = new SongListWidget.SongEntry(song, SONGS.size());
                song.entry.favorite = DiscJockey.config.favorites.contains(song.fileName);
                song.searchableFileName = song.fileName.toLowerCase().replaceAll("\\s", "");
                song.searchableName = song.name.toLowerCase().replaceAll("\\s", "");

                if (!loadNotes) {
                    // 提前返回，不解析音符数据
                    // notes 和 uniqueNotes 保持默认空值
                    return song;
                }

                short tick = -1;
                short jumps;
                ArrayList<Long> noteList = new ArrayList<>();
                HashSet<Note> uniqueSet = new HashSet<>();
                while ((jumps = reader.readShort()) != 0) {
                    tick += jumps;
                    short layer = -1;
                    while ((jumps = reader.readShort()) != 0) {
                        layer += jumps;

                        byte instrumentId = reader.readByte();
                        byte noteId = (byte) (reader.readByte() - 33);

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
                        if (uniqueSet.add(note)) {
                            song.uniqueNotes.add(note);
                        }

                        long noteLong = tick | layer << Note.LAYER_SHIFT | (long) instrumentId << Note.INSTRUMENT_SHIFT | (long) noteId << Note.NOTE_SHIFT;
                        noteList.add(noteLong);
                    }
                }
                song.notes = noteList.stream().mapToLong(Long::longValue).toArray();

                return song;
            }
        }
        return null;
    }

    public static void ensureSongLoaded(Song song) throws IOException {
        // 如果歌曲已经加载了音符数据，则直接返回
        if (song.notes != null && song.notes.length > 0) {
            return;
        }
        // 根据文件名找到对应的文件
        File songFile = null;
        for (File file : Objects.requireNonNull(DiscJockey.songsFolder.listFiles())) {
            if (file.getName().equals(song.fileName)) {
                songFile = file;
                break;
            }
        }
        if (songFile == null) {
            throw new IOException("Song file not found: " + song.fileName);
        }
        // 加载完整的歌曲数据（包括音符）
        Song fullSong = loadSong(songFile, true);
        if (fullSong == null) {
            throw new IOException("Failed to load song: " + song.fileName);
        }
        // 将音符数据复制到原歌曲对象中
        song.notes = fullSong.notes;
        song.uniqueNotes.clear();
        song.uniqueNotes.addAll(fullSong.uniqueNotes);
        // 其他字段（如length, tempo等）应该已经正确，无需覆盖
    }

    public static void sort() {
        SONGS.sort(Comparator.comparing(song -> song.displayName));
    }
}
