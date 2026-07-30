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

import semmiedev.disc_jockey.DiscJockey;
import semmiedev.disc_jockey.utils.Util;

public class RateLimiter {
    // Used to check and enforce packet rate limits to not get kicked
    private long last100MsSpanAt;
    private int last100MsSpanEstimatedPackets;
    // At how many packets/100ms should the player just reduce / stop sending packets for a while
    private long reducePacketsUntil, stopPacketsUntil;

    // Use to limit swings and look to only each tick. More will not be visually visible anyway due to interpolation
    private long lastLookSentAt, lastSwingSentAt;

    public RateLimiter() {
        reset();
    }

    public void reset() {
        last100MsSpanAt = Util.TIMESTAMP_UNINITIALIZED;
        last100MsSpanEstimatedPackets = 0;
        reducePacketsUntil = Util.TIMESTAMP_UNINITIALIZED;
        stopPacketsUntil = Util.TIMESTAMP_UNINITIALIZED;
        lastLookSentAt = Util.TIMESTAMP_UNINITIALIZED;
        lastSwingSentAt = Util.TIMESTAMP_UNINITIALIZED;
    }

    public int getMaxCosmeticPacketsPer100ms() {
        return DiscJockey.config.playbackPacketRatelimit.getReducePacketsPer100Millis();
    }

    public int getMaxPacketsPer100ms() {
        return DiscJockey.config.playbackPacketRatelimit.getMaxPacketsPer100Millis();
    }

    /**
     * Should run each tick, but does not need to.
     * Just run before running a lot of ŕate limit checks / on*Packet()
     */
    public void tick() {
        final long now = Util.now();
        if(last100MsSpanAt != Util.TIMESTAMP_UNINITIALIZED && now - last100MsSpanAt >= 100) {
            last100MsSpanEstimatedPackets = 0;
            last100MsSpanAt = now;
        }else if (last100MsSpanAt == Util.TIMESTAMP_UNINITIALIZED) {
            last100MsSpanAt = now;
            last100MsSpanEstimatedPackets = 0;
        }
    }

    public void onPacketSent() {
        last100MsSpanEstimatedPackets++;
        checkLimits();
    }

    public void onLookPacketSent() {
        lastLookSentAt = Util.now();
        onPacketSent();
    }

    public void onSwingPacketSent() {
        lastSwingSentAt = Util.now();
        onPacketSent();
    }

    public void checkLimits() {
        if(last100MsSpanEstimatedPackets >= getMaxCosmeticPacketsPer100ms()) {
            reducePacketsUntil = Math.max(reducePacketsUntil, Util.now() + 500);
        }
        if(last100MsSpanEstimatedPackets >= getMaxPacketsPer100ms()) {
            DiscJockey.LOGGER.warn("Stopping all packets for a bit!");
            final long now = Util.now();
            stopPacketsUntil = Math.max(stopPacketsUntil, now + 250);
            reducePacketsUntil = Math.max(reducePacketsUntil, now + 10000);
        }
    }

    public boolean canSendCosmeticPacket() {
        return last100MsSpanEstimatedPackets < getMaxCosmeticPacketsPer100ms() && (reducePacketsUntil == Util.TIMESTAMP_UNINITIALIZED || reducePacketsUntil < Util.now());
    }

    public boolean canSendAnyPacket() {
        return last100MsSpanEstimatedPackets < getMaxPacketsPer100ms() && (stopPacketsUntil == Util.TIMESTAMP_UNINITIALIZED || stopPacketsUntil < Util.now());
    }

    public boolean canSendLookPacket() {
        return (lastLookSentAt == Util.TIMESTAMP_UNINITIALIZED || Util.now() - lastLookSentAt >= 50) && canSendCosmeticPacket();
    }

    public boolean canSendSwingPacket() {
        return (lastSwingSentAt == Util.TIMESTAMP_UNINITIALIZED || Util.now() - lastSwingSentAt >= 50) && canSendCosmeticPacket();
    }

}
