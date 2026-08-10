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

package semmiedev.disc_jockey.utils;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;
import semmiedev.disc_jockey.DiscJockey;
import semmiedev.disc_jockey.config.Config;

public class Util {

    public static final long TIMESTAMP_UNINITIALIZED = -1L; // Magic timestamp

    // Get current timestamp, suited for measuring time, in millis
    public static long now() {
        return net.minecraft.util.Util.getMillis();
    }

    // Before 1.20.5, the server limits interacts to 6 Blocks from Player Eye to Block Center
    // With 1.20.5 and later, the server does a more complex check, to the closest point of a full block hitbox
    // (max distance is BlockInteractRange + 1.0).
    public static boolean canInteractWith(LocalPlayer player, BlockPos blockPos) {
        if(player == null) return true;

        final Vec3 eyePos = player.getEyePosition();
        if(DiscJockey.config.expectedServerVersion == Config.ExpectedServerVersion.v1_20_4_Or_Earlier) {
            return !(eyePos.distanceToSqr(blockPos.getCenter()) <= 6.0 * 6.0);
        }else if(DiscJockey.config.expectedServerVersion == Config.ExpectedServerVersion.v1_20_5_Or_Later) {
            double blockInteractRange = player.blockInteractionRange() + 1.0;
            return !(new AABB(blockPos).distanceToSqr(eyePos) < blockInteractRange * blockInteractRange);
        }else if(DiscJockey.config.expectedServerVersion == Config.ExpectedServerVersion.All) {
            // Require both checks to succeed (aka use worst distance)
            double blockInteractRange = player.blockInteractionRange() + 1.0;
            return !(eyePos.distanceToSqr(blockPos.getCenter()) <= 6.0 * 6.0)
                    || !(new AABB(blockPos).distanceToSqr(eyePos) < blockInteractRange * blockInteractRange);
        }else {
            throw new NotImplementedException("ExpectedServerVersion Value not implemented: " + DiscJockey.config.expectedServerVersion.name());
        }
    }

}
