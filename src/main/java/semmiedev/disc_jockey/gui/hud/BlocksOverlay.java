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

package semmiedev.disc_jockey.gui.hud;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
//#if MC < 26.1
import net.minecraft.client.renderer.entity.ItemRenderer;
//#endif
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ARGB;

public class BlocksOverlay {
    public static ItemStack[] itemStacks;
    public static int[] amounts;
    public static int amountOfNoteBlocks;

    private static final ItemStack NOTE_BLOCK = Blocks.NOTE_BLOCK.asItem().getDefaultInstance();

    public static void render(GuiGraphics context, DeltaTracker tickCounter) {
        if (itemStacks != null) {
            context.fill(2, 2, 62, (itemStacks.length + 1) * 20 + 7, ARGB.color(255, 22, 22, 27));
            context.fill(4, 4, 60, (itemStacks.length + 1) * 20 + 5, ARGB.color(255, 42, 42, 47));

            Minecraft client = Minecraft.getInstance();
            Font textRenderer = client.font;
            //#if MC < 26.1
            ItemRenderer itemRenderer = client.getItemRenderer();
            //#endif

            //textRenderer.draw(matrices, " × "+amountOfNoteBlocks, 26, 13, 0xFFFFFF);
            context.drawString(textRenderer, " × "+amountOfNoteBlocks, 26, 13, 0xFFFFFF, true);
            //itemRenderer.renderInGui(matrices, NOTE_BLOCK, 6, 6);
            context.renderItem(NOTE_BLOCK, 6, 6);

            for (int i = 0; i < itemStacks.length; i++) {
                //textRenderer.draw(matrices, " × "+amounts[i], 26, 13 + 20 * (i + 1), 0xFFFFFF);
                context.drawString(textRenderer, " × "+amounts[i], 26, 13 + 20 * (i + 1), 0xFFFFFF, true);
                //itemRenderer.renderInGui(matrices, itemStacks[i], 6, 6 + 20 * (i + 1));
                context.renderItem(itemStacks[i], 6, 6 + 20 * (i + 1));
            }
        }
    }
}
