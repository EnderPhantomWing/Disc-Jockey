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
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import semmiedev.disc_jockey.DiscJockey;
import semmiedev.disc_jockey.utils.Util;
//#if MC >= 26.1
//$$ import net.minecraft.client.multiplayer.chat.GuiMessageSource;
//$$ import net.minecraft.client.multiplayer.chat.GuiMessageTag;
//$$ import net.minecraft.network.chat.MessageSignature;
//#endif

import java.io.IOException;
import java.util.Objects;

//#if MC < 26.1
public class SongPlayer implements ClientTickEvents.StartWorldTick {
//#else
//$$ public class SongPlayer implements ClientTickEvents.StartLevelTick {
//#endif
    private static boolean warned;
    public boolean running;
    public Song song;

    private int index;
    private double tick; // Aka song position
    private long lastPlaybackTickAt = Util.TIMESTAMP_UNINITIALIZED;
    // The thread executing the tickPlayback method
    private Thread playbackThread = null;
    public long playbackLoopDelay = 5;
    // Just for external debugging purposes
    public float speed = 1.0f;
    public boolean didSongReachEnd = false;
    public boolean loopSong = false;
    private final RateLimiter rateLimiter = new RateLimiter();
    public final Tuner tuner = new Tuner();

    public SongPlayer() {
        DiscJockey.TICK_LISTENERS.add(this);
    }

    @SuppressWarnings("BusyWait")
    public synchronized void startPlaybackThread() {
        if(DiscJockey.config.disableAsyncPlayback) {
            playbackThread = null;
            return;
        }

        this.playbackThread = new Thread(() -> {
            Thread ownThread = this.playbackThread;
            while (ownThread == this.playbackThread) {
                try {
                    // Accuracy doesn't really matter at this precision imo
                    Thread.sleep(playbackLoopDelay);
                } catch (InterruptedException ignored) {}
                tickPlayback();
            }
        });
        this.playbackThread.start();
    }

    public synchronized void stopPlaybackThread() {
        this.playbackThread = null; // Should stop on its own then
    }

    public synchronized void start(Song song) {
        if (!DiscJockey.config.hideWarning && !warned) {
            //#if MC < 26.1
            Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("disc_jockey.warning").withStyle(ChatFormatting.BOLD, ChatFormatting.RED));
            //#elseif MC >= 26.1 && MC < 26.2
            //$$ Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("disc_jockey.warning").withStyle(ChatFormatting.BOLD, ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
            //#else
            //$$ Minecraft.getInstance().gui.hud.getChat().addMessage(Component.translatable("disc_jockey.warning").withStyle(ChatFormatting.BOLD, ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
            //#endif
            warned = true;
            return;
        }
        if (running) stop();
        tick = 0;
        index = 0;
        this.song = song;
        // 确保歌曲的音符数据已加载
        try {
            SongLoader.ensureSongLoaded(song);
        } catch (IOException e) {
            DiscJockey.LOGGER.error("Failed to load song data for {}", song.fileName, e);
            // 不开始播放
            return;
        }
        //DiscJockey.LOGGER.info("Song length: " + song.length + " and tempo " + song.tempo);
        if(this.playbackThread == null) startPlaybackThread();
        running = true;
        rateLimiter.reset();
        tuner.reset();
        didSongReachEnd = false;
    }

    public synchronized void stop() {
        stopPlaybackThread();
        running = false;
        index = 0;
        tick = 0;
        rateLimiter.reset();
        tuner.reset();
        didSongReachEnd = false; // Change after running stop() if actually ended cleanly
    }

    /**
     * Can be run from both a separate thread or on minecraft ticks. Decided by DiscJockey.config.disableAsyncPlayback
     */
    public synchronized void tickPlayback() {
        if (!running) {
            lastPlaybackTickAt = Util.TIMESTAMP_UNINITIALIZED;
            rateLimiter.reset();
            return;
        }
        long previousPlaybackTickAt = lastPlaybackTickAt;
        lastPlaybackTickAt = Util.now();
        rateLimiter.tick();

        if(!tuner.isTuned()) return;

        while (running) {
            Minecraft client = Minecraft.getInstance();
            GameType gameMode = client.gameMode == null ? null : client.gameMode.getPlayerMode();
            // In the best case, gameMode would only be queried in sync Ticks, no here
            if (gameMode == null || !gameMode.isSurvival()) {
                //#if MC < 26.1
                client.gui.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID+".player.invalid_game_mode", gameMode == null ? "unknown" : gameMode.getLongDisplayName()).withStyle(ChatFormatting.RED));
                //#elseif MC >= 26.1 && MC < 26.2
                //$$ client.gui.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID+".player.invalid_game_mode", gameMode == null ? "unknown" : gameMode.getLongDisplayName()).withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
                //#else
                //$$ client.gui.hud.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID+".player.invalid_game_mode", gameMode == null ? "unknown" : gameMode.getLongDisplayName()).withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
                //#endif
                stop();
                return;
            }

            long note = song.notes[index];
            if ((short)note <= Math.round(tick)) {
                var instrumentMap = tuner.getNoteBlocks().get(Note.INSTRUMENTS[(byte)(note >> Note.INSTRUMENT_SHIFT)]);
                if (instrumentMap == null) {
                    // Instrument got likely mapped to "nothing". Skip it
                    index++;
                    continue;
                }
                @Nullable BlockPos blockPos = instrumentMap.get((byte)(note >> Note.NOTE_SHIFT));
                if(blockPos == null) {
                    // Instrument got likely mapped to "nothing". Skip it
                    index++;
                    continue;
                }
                if (Util.canInteractWith(client.player, blockPos)) {
                    stop();
                    //#if MC < 26.1
                    client.gui.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID+".player.too_far").withStyle(ChatFormatting.RED));
                    //#elseif MC >= 26.1 && MC < 26.2
                    //$$ client.gui.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID+".player.too_far").withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
                    //#else
                    //$$ client.gui.hud.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID+".player.too_far").withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
                    //#endif
                    return;
                }
                Vec3 unit = Vec3.upFromBottomCenterOf(blockPos, 0.5).subtract(client.player.getEyePosition()).normalize();
                if(rateLimiter.canSendLookPacket()) {
                    Objects.requireNonNull(client.getConnection()).send(new ServerboundMovePlayerPacket.Rot(Mth.wrapDegrees((float) (Mth.atan2(unit.z, unit.x) * 57.2957763671875) - 90.0f), Mth.wrapDegrees((float) (-(Mth.atan2(unit.y, Math.sqrt(unit.x * unit.x + unit.z * unit.z)) * 57.2957763671875))), client.player.onGround(), client.player.horizontalCollision));
                    rateLimiter.onLookPacketSent();
                }
                if(rateLimiter.canSendAnyPacket()) {
                    // TODO: 5/30/2022 Check if the block needs tuning
                    client.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP, 0));
                    rateLimiter.onPacketSent();
                }
                if(rateLimiter.canSendCosmeticPacket()) {
                    client.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, blockPos, Direction.UP, 0));
                    rateLimiter.onPacketSent();
                }
                if(rateLimiter.canSendSwingPacket()) {
                    client.executeIfPossible(() -> client.player.swing(InteractionHand.MAIN_HAND));
                    rateLimiter.onSwingPacketSent();
                }

                index++;
                if (index >= song.notes.length) {
                    stop();
                    didSongReachEnd = true;
                    if(loopSong) {
                        start(song);
                    }
                    break;
                }
            } else {
                break;
            }
        }

        if(running) { // Might not be running anymore (prevent small offset on song, even if that is not played anymore)
            long elapsedMs = previousPlaybackTickAt != -1L && lastPlaybackTickAt != -1L ? lastPlaybackTickAt - previousPlaybackTickAt : 16; // Assume 16ms if unknown
            tick += song.millisecondsToTicks(elapsedMs) * speed;
        }
    }

    @Override
    public void onStartTick(@NonNull ClientLevel world) {
        Minecraft client = Minecraft.getInstance();
        if(client.level == null || client.player == null) return;
        if(song == null || !running) return;

        tuner.cleanup(); // Housekeeping

        // Select song
        if (!tuner.isSongSelected()) {
            if (!tuner.selectSong(client, song)) {
                if(!tuner.getMissingInstrumentBlocks().isEmpty()) {
                    //#if MC < 26.2
                    ChatComponent chatHud = Minecraft.getInstance().gui.getChat();
                    //#else
                    //$$ ChatComponent chatHud = Minecraft.getInstance().gui.hud.getChat();
                    //#endif

                    //#if MC < 26.1
                    chatHud.addMessage(Component.translatable(DiscJockey.MOD_ID + ".player.invalid_note_blocks").withStyle(ChatFormatting.RED));
                    tuner.getMissingInstrumentBlocks().forEach((block, integer) -> chatHud.addMessage(Component.literal(block.getName().getString() + " × " + integer).withStyle(ChatFormatting.RED)));
                    //#else
                    //$$ chatHud.addMessage(Component.translatable(DiscJockey.MOD_ID + ".player.invalid_note_blocks").withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
                    //$$ tuner.getMissingInstrumentBlocks().forEach((block, integer) -> chatHud.addMessage(Component.literal(block.getName().getString() + " × " + integer).withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError()));
                    //#endif
                    stop();
                    return;
                }else {
                    DiscJockey.LOGGER.error("Failed to select song to unknown / unexpected reason!");
                    //#if MC < 26.1
                    client.gui.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID + ".selectsong_fail_unknown").withStyle(ChatFormatting.RED));
                    //#elseif MC >= 26.1 && MC < 26.2
                    //$$ client.gui.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID + ".selectsong_fail_unknown").withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
                    //#else
                    //$$ client.gui.hud.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID + ".selectsong_fail_unknown").withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
                    //#endif
                    stop();
                    return;
                }
            }else {
                DiscJockey.LOGGER.info("Selected song: " + song.displayName + " (" + song.fileName + ")");
            }
        }

        // Tune
        if (!tuner.isTuned()) {
            Tuner.TuningFail tuningFail = tuner.tickTuning(client);
            if (tuningFail == Tuner.TuningFail.MovedTooFarAway) {
                stop();
                //#if MC < 26.1
                client.gui.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID + ".player.too_far").withStyle(ChatFormatting.RED));
                //#elseif MC >= 26.1 && MC < 26.2
                //$$ client.gui.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID + ".player.too_far").withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
                //#else
                //$$ client.gui.hud.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID + ".player.too_far").withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
                //#endif
                return;
            } else if(tuningFail != null) {
                stop();
                DiscJockey.LOGGER.error("Tuning song failed: " + tuningFail.name());
                //#if MC < 26.1
                client.gui.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID + ".player.tuning_fail_other", tuningFail.name()).withStyle(ChatFormatting.RED));
                //#elseif MC >= 26.1 && MC < 26.2
                //$$ client.gui.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID + ".player.tuning_fail_other", tuningFail.name()).withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
                //#else
                //$$ client.gui.hud.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID + ".player.tuning_fail_other", tuningFail.name()).withStyle(ChatFormatting.RED), (MessageSignature)null, GuiMessageSource.PLAYER, GuiMessageTag.chatError());
                //#endif
                return;
            }
        }

        if(tuner.isTuned() && (playbackThread == null || !playbackThread.isAlive()) && running && DiscJockey.config.disableAsyncPlayback) {
            // Sync playback (off by default). Replacement for playback thread
            try {
                tickPlayback();
            }catch (Exception ex) {
                DiscJockey.LOGGER.error("Failed to tick playback synchronously!", ex);
                stop();
            }
        }
    }

    public void setSongElapsedSeconds(double seconds) {
        tick = song.millisecondsToTicks((long) seconds * 1000);
        index = 0;
        for(int i = 0; i < song.notes.length; i++) {
            long note = song.notes[i];
            if((short) note >= Math.round(tick)) {
                index = i;
                //DiscJockey.LOGGER.info("Seconds: " + seconds + ", Tick: " + tick + ", Index: " + index);
                break;
            }
        }
    }

    public double getSongElapsedSeconds() {
        if(song == null) return 0;
        return song.ticksToMilliseconds(tick) / 1000;
    }
}
