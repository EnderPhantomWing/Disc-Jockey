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

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MetaMessage;
import java.io.File;
import java.util.*;

import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public class MidiLoader {
    // Record to hold raw MIDI note data before octave adjustment
    private record NoteData(long midiTick, int channel, int originalPitch, int velocity, int program, int bankMSB, int bankLSB) {}

    // Maps General MIDI instrument program IDs to Minecraft Note Block instruments.
    private static final Map<Integer, NoteBlockInstrument> INSTRUMENT_MAP = new HashMap<>();
    // Maps (bank << 8) | program to NoteBlockInstrument for GS/XG extended banks.
    private static final Map<Integer, NoteBlockInstrument> BANKED_INSTRUMENT_MAP = new HashMap<>();

    static {
        INSTRUMENT_MAP.put(0, NoteBlockInstrument.HARP); // 0
        INSTRUMENT_MAP.put(1, NoteBlockInstrument.PLING); // 1
        INSTRUMENT_MAP.put(2, NoteBlockInstrument.PLING); // 2
        INSTRUMENT_MAP.put(3, NoteBlockInstrument.PLING); // 3
        INSTRUMENT_MAP.put(4, NoteBlockInstrument.HARP); // 4
        INSTRUMENT_MAP.put(5, NoteBlockInstrument.HARP); // 5
        INSTRUMENT_MAP.put(6, NoteBlockInstrument.GUITAR); // 6
        INSTRUMENT_MAP.put(7, NoteBlockInstrument.BANJO); // 7
        INSTRUMENT_MAP.put(8, NoteBlockInstrument.BELL); // 8
        INSTRUMENT_MAP.put(9, NoteBlockInstrument.BELL); // 9
        INSTRUMENT_MAP.put(10, NoteBlockInstrument.BELL); // 10
        INSTRUMENT_MAP.put(11, NoteBlockInstrument.IRON_XYLOPHONE); // 11
        INSTRUMENT_MAP.put(12, NoteBlockInstrument.IRON_XYLOPHONE); // 12
        INSTRUMENT_MAP.put(13, NoteBlockInstrument.XYLOPHONE); // 13
        INSTRUMENT_MAP.put(14, NoteBlockInstrument.BELL); // 14
        INSTRUMENT_MAP.put(15, NoteBlockInstrument.GUITAR); // 15
        INSTRUMENT_MAP.put(16, NoteBlockInstrument.FLUTE); // 16
        INSTRUMENT_MAP.put(17, NoteBlockInstrument.IRON_XYLOPHONE); // 17
        INSTRUMENT_MAP.put(18, NoteBlockInstrument.FLUTE); // 18
        INSTRUMENT_MAP.put(19, NoteBlockInstrument.FLUTE); // 19
        INSTRUMENT_MAP.put(20, NoteBlockInstrument.FLUTE); // 20
        INSTRUMENT_MAP.put(21, NoteBlockInstrument.FLUTE); // 21
        INSTRUMENT_MAP.put(22, NoteBlockInstrument.FLUTE); // 22
        INSTRUMENT_MAP.put(23, NoteBlockInstrument.FLUTE); // 23
        INSTRUMENT_MAP.put(24, NoteBlockInstrument.GUITAR); // 24
        INSTRUMENT_MAP.put(25, NoteBlockInstrument.GUITAR); // 25
        INSTRUMENT_MAP.put(26, NoteBlockInstrument.HARP); // 26
        INSTRUMENT_MAP.put(27, NoteBlockInstrument.GUITAR); // 27
        INSTRUMENT_MAP.put(28, NoteBlockInstrument.BASS); // 28
        INSTRUMENT_MAP.put(29, NoteBlockInstrument.DIDGERIDOO); // 29
        INSTRUMENT_MAP.put(30, NoteBlockInstrument.DIDGERIDOO); // 30
        INSTRUMENT_MAP.put(31, NoteBlockInstrument.GUITAR); // 31
        INSTRUMENT_MAP.put(32, NoteBlockInstrument.BASS); // 32
        INSTRUMENT_MAP.put(33, NoteBlockInstrument.BASS); // 33
        INSTRUMENT_MAP.put(34, NoteBlockInstrument.BASS); // 34
        INSTRUMENT_MAP.put(35, NoteBlockInstrument.BASS); // 35
        INSTRUMENT_MAP.put(36, NoteBlockInstrument.GUITAR); // 36
        INSTRUMENT_MAP.put(37, NoteBlockInstrument.GUITAR); // 37
        INSTRUMENT_MAP.put(38, NoteBlockInstrument.BASS); // 38
        INSTRUMENT_MAP.put(39, NoteBlockInstrument.PLING); // 39
        INSTRUMENT_MAP.put(40, NoteBlockInstrument.FLUTE); // 40
        INSTRUMENT_MAP.put(41, NoteBlockInstrument.FLUTE); // 41
        INSTRUMENT_MAP.put(42, NoteBlockInstrument.FLUTE); // 42
        INSTRUMENT_MAP.put(43, NoteBlockInstrument.FLUTE); // 43
        INSTRUMENT_MAP.put(44, NoteBlockInstrument.FLUTE); // 44
        INSTRUMENT_MAP.put(45, NoteBlockInstrument.BASS); // 45
        INSTRUMENT_MAP.put(46, NoteBlockInstrument.HARP); // 46
        INSTRUMENT_MAP.put(47, NoteBlockInstrument.SNARE); // 47
        INSTRUMENT_MAP.put(48, NoteBlockInstrument.FLUTE); // 48
        INSTRUMENT_MAP.put(49, NoteBlockInstrument.FLUTE); // 49
        INSTRUMENT_MAP.put(50, NoteBlockInstrument.FLUTE); // 50
        INSTRUMENT_MAP.put(51, NoteBlockInstrument.FLUTE); // 51
        INSTRUMENT_MAP.put(52, NoteBlockInstrument.FLUTE); // 52
        INSTRUMENT_MAP.put(53, NoteBlockInstrument.FLUTE); // 53
        INSTRUMENT_MAP.put(54, NoteBlockInstrument.FLUTE); // 54
        INSTRUMENT_MAP.put(55, NoteBlockInstrument.SNARE); // 55
        INSTRUMENT_MAP.put(56, NoteBlockInstrument.FLUTE); // 56
        INSTRUMENT_MAP.put(57, NoteBlockInstrument.FLUTE); // 57
        INSTRUMENT_MAP.put(58, NoteBlockInstrument.FLUTE); // 58
        INSTRUMENT_MAP.put(59, NoteBlockInstrument.DIDGERIDOO); // 59
        INSTRUMENT_MAP.put(60, NoteBlockInstrument.FLUTE); // 60
        INSTRUMENT_MAP.put(61, NoteBlockInstrument.DIDGERIDOO); // 61
        INSTRUMENT_MAP.put(62, NoteBlockInstrument.DIDGERIDOO); // 62
        INSTRUMENT_MAP.put(63, NoteBlockInstrument.FLUTE); // 63
        INSTRUMENT_MAP.put(64, NoteBlockInstrument.FLUTE); // 64
        INSTRUMENT_MAP.put(65, NoteBlockInstrument.FLUTE); // 65
        INSTRUMENT_MAP.put(66, NoteBlockInstrument.FLUTE); // 66
        INSTRUMENT_MAP.put(67, NoteBlockInstrument.FLUTE); // 67
        INSTRUMENT_MAP.put(68, NoteBlockInstrument.FLUTE); // 68
        INSTRUMENT_MAP.put(69, NoteBlockInstrument.FLUTE); // 69
        INSTRUMENT_MAP.put(70, NoteBlockInstrument.FLUTE); // 70
        INSTRUMENT_MAP.put(71, NoteBlockInstrument.FLUTE); // 71
        INSTRUMENT_MAP.put(72, NoteBlockInstrument.FLUTE); // 72
        INSTRUMENT_MAP.put(73, NoteBlockInstrument.FLUTE); // 73
        INSTRUMENT_MAP.put(74, NoteBlockInstrument.FLUTE); // 74
        INSTRUMENT_MAP.put(75, NoteBlockInstrument.FLUTE); // 75
        INSTRUMENT_MAP.put(76, NoteBlockInstrument.FLUTE); // 76
        INSTRUMENT_MAP.put(77, NoteBlockInstrument.FLUTE); // 77
        INSTRUMENT_MAP.put(78, NoteBlockInstrument.FLUTE); // 78
        INSTRUMENT_MAP.put(79, NoteBlockInstrument.FLUTE); // 79
        INSTRUMENT_MAP.put(80, NoteBlockInstrument.BIT); // 80
        INSTRUMENT_MAP.put(81, NoteBlockInstrument.FLUTE); // 81
        INSTRUMENT_MAP.put(82, NoteBlockInstrument.FLUTE); // 82
        INSTRUMENT_MAP.put(83, NoteBlockInstrument.FLUTE); // 83
        INSTRUMENT_MAP.put(84, NoteBlockInstrument.GUITAR); // 84
        INSTRUMENT_MAP.put(85, NoteBlockInstrument.FLUTE); // 85
        INSTRUMENT_MAP.put(86, NoteBlockInstrument.FLUTE); // 86
        INSTRUMENT_MAP.put(87, NoteBlockInstrument.BASS); // 87
        INSTRUMENT_MAP.put(88, NoteBlockInstrument.BELL); // 88
        INSTRUMENT_MAP.put(89, NoteBlockInstrument.FLUTE); // 89
        INSTRUMENT_MAP.put(90, NoteBlockInstrument.FLUTE); // 90
        INSTRUMENT_MAP.put(91, NoteBlockInstrument.FLUTE); // 91
        INSTRUMENT_MAP.put(92, NoteBlockInstrument.FLUTE); // 92
        INSTRUMENT_MAP.put(93, NoteBlockInstrument.FLUTE); // 93
        INSTRUMENT_MAP.put(94, NoteBlockInstrument.FLUTE); // 94
        INSTRUMENT_MAP.put(95, NoteBlockInstrument.CHIME); // 95
        INSTRUMENT_MAP.put(96, NoteBlockInstrument.CHIME); // 96
        INSTRUMENT_MAP.put(97, NoteBlockInstrument.FLUTE); // 97
        INSTRUMENT_MAP.put(98, NoteBlockInstrument.CHIME); // 98
        INSTRUMENT_MAP.put(99, NoteBlockInstrument.GUITAR); // 99
        INSTRUMENT_MAP.put(100, NoteBlockInstrument.PLING); // 100
        INSTRUMENT_MAP.put(101, NoteBlockInstrument.FLUTE); // 101
        INSTRUMENT_MAP.put(102, NoteBlockInstrument.FLUTE); // 102
        INSTRUMENT_MAP.put(103, NoteBlockInstrument.GUITAR); // 103
        INSTRUMENT_MAP.put(104, NoteBlockInstrument.BANJO); // 104
        INSTRUMENT_MAP.put(105, NoteBlockInstrument.BANJO); // 105
        INSTRUMENT_MAP.put(106, NoteBlockInstrument.BANJO); // 106
        INSTRUMENT_MAP.put(107, NoteBlockInstrument.GUITAR); // 107
        INSTRUMENT_MAP.put(108, NoteBlockInstrument.IRON_XYLOPHONE); // 108
        INSTRUMENT_MAP.put(109, NoteBlockInstrument.FLUTE); // 109
        INSTRUMENT_MAP.put(110, NoteBlockInstrument.FLUTE); // 110
        INSTRUMENT_MAP.put(111, NoteBlockInstrument.FLUTE); // 111
        INSTRUMENT_MAP.put(112, NoteBlockInstrument.CHIME); // 112
        INSTRUMENT_MAP.put(113, NoteBlockInstrument.COW_BELL); // 113
        INSTRUMENT_MAP.put(114, NoteBlockInstrument.IRON_XYLOPHONE); // 114
        INSTRUMENT_MAP.put(115, NoteBlockInstrument.XYLOPHONE); // 115
        INSTRUMENT_MAP.put(116, NoteBlockInstrument.BASEDRUM); // 116
        INSTRUMENT_MAP.put(117, NoteBlockInstrument.SNARE); // 117
        INSTRUMENT_MAP.put(118, NoteBlockInstrument.SNARE); // 118
        INSTRUMENT_MAP.put(119, NoteBlockInstrument.CHIME); // 119
        INSTRUMENT_MAP.put(120, NoteBlockInstrument.HAT); // 120
        INSTRUMENT_MAP.put(121, NoteBlockInstrument.FLUTE); // 121
        INSTRUMENT_MAP.put(122, NoteBlockInstrument.CHIME); // 122
        INSTRUMENT_MAP.put(123, NoteBlockInstrument.FLUTE); // 123
        INSTRUMENT_MAP.put(124, NoteBlockInstrument.BELL); // 124
        INSTRUMENT_MAP.put(125, NoteBlockInstrument.BASEDRUM); // 125
        INSTRUMENT_MAP.put(126, NoteBlockInstrument.SNARE); // 126
        INSTRUMENT_MAP.put(127, NoteBlockInstrument.SNARE); // 127
    }

    static {
        // Copy GM mappings (bank 0) into BANKED_INSTRUMENT_MAP
        for (int program = 0; program < 128; program++) {
            NoteBlockInstrument instrument = INSTRUMENT_MAP.get(program);
            if (instrument != null) {
                BANKED_INSTRUMENT_MAP.put((0) | program, instrument);
            }
        }
        // TODO: Add GS/XG extended bank mappings here
    }

    // Returns the appropriate NoteBlockInstrument for a given bank MSB and program.
    private static NoteBlockInstrument getInstrumentForBank(int bankMSB, int program) {
        // Map all non-percussion instruments to HARP
        return NoteBlockInstrument.HARP;
    }

    // Maps MIDI percussion keys (pitch on channel 9) to Minecraft drum sounds.
    private static NoteBlockInstrument getPercussionInstrument(int midiPitch) {
        return switch (midiPitch) {
            case 35, 36, 41, 43, 45, 47, 48, 50 -> NoteBlockInstrument.BASEDRUM; // Acoustic Bass Drum, Bass Drum 1, etc.
            case 38, 40 -> NoteBlockInstrument.SNARE; // Acoustic Snare, Electric Snare
            case 42, 44, 46, 49, 51, 52, 53, 57, 59 -> NoteBlockInstrument.HAT; // Closed Hi-Hat, Pedal Hi-Hat, etc.
            case 56 -> NoteBlockInstrument.COW_BELL; // Cowbell
            case 70 -> NoteBlockInstrument.BELL; // Maracas
            case 60 -> NoteBlockInstrument.GUITAR; // Hi Bongo -> using a somewhat similar sound
            case 63 -> NoteBlockInstrument.BASS; // Open Hi Conga -> using a somewhat similar sound
            default -> NoteBlockInstrument.HAT; // Default to hi-hat for unmapped percussion
        };
    }

    // Helper method to extract tick from a note long value
    private static int getTickFromLong(long noteLong) {
        return (int)(noteLong & 0xFFFF); // Extract the first 16 bits
    }

    /**
     * Loads a MIDI file and converts it into a Song object.
     *
     * @param midiFile The MIDI file to load.
     * @return A Song object representing the MIDI file.
     * @throws Exception If an error occurs during loading or parsing.
     */
    public static Song loadFromMidi(File midiFile) throws Exception {
        Sequence sequence = MidiSystem.getSequence(midiFile);

        // --- Tempo (BPM) Calculation ---
        // Collect all Set Tempo events
        List<Song.TempoChange> tempoChanges = new ArrayList<>();
        long defaultMspqn = 500000; // default 120 BPM
        tempoChanges.add(new Song.TempoChange(0, defaultMspqn));
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof MetaMessage metaMessage) {
                    if (metaMessage.getType() == 0x51) { // Set Tempo Meta-event
                        byte[] data = metaMessage.getData();
                        long mspqn = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                        long tick = event.getTick();
                        // Insert maintaining sorted order (assuming events are encountered in order)
                        // Simpler: collect then sort
                        tempoChanges.add(new Song.TempoChange(tick, mspqn));
                    }
                }
            }
        }
        // Sort by tick
        tempoChanges.sort(Comparator.comparingLong(Song.TempoChange::midiTick));
        // Remove duplicates (same tick) , keeping the last occurrence.
        Map<Long, Long> uniqueMap = new HashMap<>();
        for (Song.TempoChange tc : tempoChanges) {
            uniqueMap.put(tc.midiTick(), tc.MSpec());
        }
        tempoChanges.clear();
        uniqueMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> tempoChanges.add(new Song.TempoChange(e.getKey(), e.getValue())));

        // For backward compatibility, compute average BPM? We'll keep first tempo for now.
        long mspqn = tempoChanges.getFirst().MSpec();
        double bpm = 60000000.0 / mspqn;
        short tempo = (short) Math.round((bpm / 60.0) * 10.0 * 100.0); // temporary, will be recalculated later

        int ppq = sequence.getResolution(); // Pulses (ticks) per quarter note

        // --- Time Signature (for bar calculation) ---
        int numerator = 4;
        int denominator = 4;
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof MetaMessage metaMessage) {
                    if (metaMessage.getType() == 0x58) { // Time Signature meta-event
                        byte[] data = metaMessage.getData();
                        numerator = data[0] & 0xFF;
                        denominator = 1 << (data[1] & 0xFF); // 2^denomIndex
                        // data[2] and data[3] are ignored (clocks per click, 32nd notes per quarter)
                        break;
                    }
                }
            }
        }
        // Calculate ticks per bar: ppq * 4 * (numerator / denominator)
        double quarterNotesPerBar = 4.0 * numerator / denominator;
        int ticksPerBar = (int) Math.round(ppq * quarterNotesPerBar);
        int windowSizeBars = 2; // 2 bars per window
        int windowTicks = ticksPerBar * windowSizeBars;
        if (windowTicks == 0) windowTicks = ppq * 4 * 2; // fallback

        // --- Note Processing ---
        // Pre‑compute cumulative microseconds for each tempo change
        long[] tempoTicks = new long[tempoChanges.size()];
        long[] cumulativeMicros = new long[tempoChanges.size()];
        long lastTick = 0;
        long lastMspqn = tempoChanges.getFirst().MSpec();
        long cumul = 0;
        for (int idx = 0; idx < tempoChanges.size(); idx++) {
            Song.TempoChange tc = tempoChanges.get(idx);
            long tick = tc.midiTick();
            long currentMspqn = tc.MSpec();
            // microseconds from lastTick to this tick using previous tempo
            if (tick > lastTick) {
                cumul += (tick - lastTick) * lastMspqn / ppq;
            }
            tempoTicks[idx] = tick;
            cumulativeMicros[idx] = cumul;
            lastTick = tick;
            lastMspqn = currentMspqn;
        }
        // Helper to convert MIDI tick to microseconds
        java.util.function.LongUnaryOperator tickToMicros = (midiTick) -> {
            // binary search for the last tempo change with tick <= midiTick
            int lo = 0, hi = tempoTicks.length - 1;
            int best = 0;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                if (tempoTicks[mid] <= midiTick) {
                    best = mid;
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
            long baseTick = tempoTicks[best];
            long baseMicros = cumulativeMicros[best];
            long tempoMspqn = tempoChanges.get(best).MSpec();
            long extraTicks = midiTick - baseTick;
            return baseMicros + extraTicks * tempoMspqn / ppq;
        };

        List<Long> noteLongs = new ArrayList<>();
        long maxMidiTick = 0;

        int[] channelInstruments = new int[16];
        int[] channelBankMSB = new int[16]; // Controller 0
        int[] channelBankLSB = new int[16]; // Controller 32

        // First pass: collect raw note data
        List<NoteData> rawNotes = new ArrayList<>();
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage sm) {
                    int channel = sm.getChannel();
                    int command = sm.getCommand();
                    int data1 = sm.getData1(); // Note pitch
                    int data2 = sm.getData2(); // Velocity

                    if (command == ShortMessage.CONTROL_CHANGE) {
                        if (data1 == 0) {
                            channelBankMSB[channel] = data2 & 0x7F;
                        } else if (data1 == 32) {
                            channelBankLSB[channel] = data2 & 0x7F;
                        }
                    } else if (command == ShortMessage.PROGRAM_CHANGE) {
                        channelInstruments[channel] = data1;
                    } else if (command == ShortMessage.NOTE_ON && data2 > 0) { // Note On with velocity > 0
                        long midiTick = event.getTick();
                        if (midiTick > maxMidiTick) maxMidiTick = midiTick;
                        int program = channelInstruments[channel];
                        rawNotes.add(new NoteData(midiTick, channel, data1, data2, program, channelBankMSB[channel], channelBankLSB[channel]));
                    }
                }
            }
        }

        // If no notes, return empty song
        if (rawNotes.isEmpty()) {
            Song emptySong = new Song();
            String name = midiFile.getName().substring(0, midiFile.getName().lastIndexOf('.'));
            emptySong.fileName = midiFile.getName();
            emptySong.name = name;
            emptySong.displayName = name;
            emptySong.length = 0;
            emptySong.tempo = tempo;
            emptySong.notes = new long[0];
            return emptySong;
        }

        // Compute window-based octave offsets per channel (excluding percussion channel 9)
        int windowCount = (int) ((maxMidiTick / windowTicks) + 1);
        // Map: windowIndex -> channel -> list of pitches
        Map<Integer, Map<Integer, List<Integer>>> windowPitchMap = new HashMap<>();
        for (NoteData nd : rawNotes) {
            if (nd.channel() == 9) continue; // skip percussion
            int windowIdx = (int) (nd.midiTick() / windowTicks);
            windowPitchMap
                    .computeIfAbsent(windowIdx, k -> new HashMap<>())
                    .computeIfAbsent(nd.channel(), k -> new ArrayList<>())
                    .add(nd.originalPitch());
        }

        // Compute average pitch per window per channel and determine octave offset
        Map<Integer, Map<Integer, Integer>> windowChannelOffset = new HashMap<>();
        int lowBound = 54; // F#3
        int highBound = 78; // F#5
        int targetCenter = 66; // middle of range (F#4)
        for (var windowEntry : windowPitchMap.entrySet()) {
            int windowIdx = windowEntry.getKey();
            Map<Integer, List<Integer>> channelMap = windowEntry.getValue();
            Map<Integer, Integer> channelOffset = new HashMap<>();
            for (var channelEntry : channelMap.entrySet()) {
                int channel = channelEntry.getKey();
                List<Integer> pitches = channelEntry.getValue();
                // compute min, max, average
                int minPitch = 127;
                int maxPitch = 0;
                double sum = 0;
                for (int p : pitches) {
                    if (p < minPitch) minPitch = p;
                    if (p > maxPitch) maxPitch = p;
                    sum += p;
                }
                double average = sum / pitches.size();
                // find best offset among possible octave shifts (-4 to +4 octaves)
                int bestOffset = 0;
                int bestViolation = Integer.MAX_VALUE;
                double bestCenterDist = Double.MAX_VALUE;
                for (int oct = -4; oct <= 4; oct++) {
                    int offset = oct * 12;
                    int shiftedMin = minPitch + offset;
                    int shiftedMax = maxPitch + offset;
                    // compute violation: amount outside bounds (0 if inside)
                    int violation = 0;
                    if (shiftedMin < lowBound) violation += lowBound - shiftedMin;
                    if (shiftedMax > highBound) violation += shiftedMax - highBound;
                    // distance of shifted average from target center
                    double shiftedAvg = average + offset;
                    double centerDist = Math.abs(shiftedAvg - targetCenter);
                    // select offset with minimal violation, then minimal center distance
                    if (violation < bestViolation || (violation == bestViolation && centerDist < bestCenterDist)) {
                        bestViolation = violation;
                        bestCenterDist = centerDist;
                        bestOffset = offset;
                    }
                }
                // Limit offset to avoid extreme shifts (already limited to ±4 octaves)
                channelOffset.put(channel, bestOffset);
            }
            windowChannelOffset.put(windowIdx, channelOffset);
        }

        // Second pass: convert notes to noteLongs with octave adjustment
        for (NoteData nd : rawNotes) {
            long midiTick = nd.midiTick();
            int channel = nd.channel();
            int originalPitch = nd.originalPitch();
            int program = nd.program();
            int bankMSB = nd.bankMSB();
            int bankLSB = nd.bankLSB();

            // Apply window-specific octave offset if applicable
            int adjustedPitch = originalPitch;
            if (channel != 9) {
                int windowIdx = (int) (midiTick / windowTicks);
                var channelOffsetMap = windowChannelOffset.get(windowIdx);
                if (channelOffsetMap != null) {
                    Integer offset = channelOffsetMap.get(channel);
                    if (offset != null) {
                        adjustedPitch = originalPitch + offset;
                        // Clamp to MIDI pitch range 0-127
                        if (adjustedPitch < 0) adjustedPitch = 0;
                        if (adjustedPitch > 127) adjustedPitch = 127;
                    }
                }
            }

            // Convert adjusted pitch to Minecraft note ID using NBS octave adjustment
            int note = adjustedPitch;
            while (note < 54) note += 12; // F#3
            while (note > 78) note -= 12; // F#5
            int noteId = note - 54; // now in range 0-24

            NoteBlockInstrument instrument;
            if (channel == 9) { // MIDI channel 10 (0-indexed 9) is for percussion
                instrument = getPercussionInstrument(originalPitch);
            } else {
                instrument = getInstrumentForBank(bankMSB, program);
            }
            int instrumentId = instrument.ordinal();

            // Convert MIDI tick to song tick (20 ticks per second) with variable tempo
            long microseconds = tickToMicros.applyAsLong(midiTick);
            double timeInMs = microseconds / 1000.0;
            int songTick = (int)Math.round(timeInMs / 50.0);

            // Layer is unused for MIDI files, defaulting to 0
            short layer = 0;

            // Encode note data using project's format
            long noteLong = (long) songTick | (long) instrumentId << Note.INSTRUMENT_SHIFT | (long) noteId << Note.NOTE_SHIFT;
            noteLongs.add(noteLong);
        }

        // Sort notes by tick
        noteLongs.sort(Comparator.comparingInt(MidiLoader::getTickFromLong));

        String name = midiFile.getName().substring(0, midiFile.getName().lastIndexOf('.'));
        short length = noteLongs.isEmpty() ? 0 : (short) getTickFromLong(noteLongs.getLast());

        // Recalculate tempo based on actual duration (using variable tempo)
        if (maxMidiTick > 0) {
            long totalMicroseconds = tickToMicros.applyAsLong(maxMidiTick);
            double totalSeconds = totalMicroseconds / 1_000_000.0;
            double ticksPerSecond = length / totalSeconds; // song ticks per second
            tempo = (short) Math.round(ticksPerSecond * 100.0);
        }

        // Create a new Song instance
        Song song = new Song();
        song.fileName = midiFile.getName();
        song.name = name;
        song.displayName = name;
        song.length = length;
        song.midiPpq = ppq;
        song.tempoChanges = tempoChanges;
        song.tempo = tempo;
        song.notes = noteLongs.stream().mapToLong(Long::longValue).toArray();

        // Populate uniqueNotes
        Set<Note> seen = new HashSet<>();
        for (long noteLong : noteLongs) {
            byte instrumentId = (byte)(noteLong >> Note.INSTRUMENT_SHIFT);
            byte noteId = (byte)(noteLong >> Note.NOTE_SHIFT);
            Note note = new Note(Note.INSTRUMENTS[instrumentId], noteId);
            if (seen.add(note)) {
                song.uniqueNotes.add(note);
            }
        }

        return song;
    }
}
