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

package semmiedev.disc_jockey;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;
import semmiedev.disc_jockey.command.DiscjockeyCommand;
import semmiedev.disc_jockey.config.Config;
import semmiedev.disc_jockey.disc.Previewer;
import semmiedev.disc_jockey.disc.SongLoader;
import semmiedev.disc_jockey.disc.SongPlayer;
import semmiedev.disc_jockey.gui.hud.BlocksOverlay;
import semmiedev.disc_jockey.gui.screen.DiscJockeyScreen;

import java.io.File;
import java.util.ArrayList;

public class DiscJockey implements ClientModInitializer {
    public static final String MOD_ID = "disc_jockey";
    public static final MutableComponent NAME = Component.literal("Disc Jockey");
    public static final Logger LOGGER = LogManager.getLogger("Disc Jockey");
    public static final ArrayList<ClientTickEvents.StartWorldTick> TICK_LISTENERS = new ArrayList<>();
    public static final Previewer PREVIEWER = new Previewer();
    public static final SongPlayer SONG_PLAYER = new SongPlayer();

    public static File songsFolder;
    public static Config config;
    public static ConfigHolder<Config> configHolder;

    @Override
    public void onInitializeClient() {
        configHolder = AutoConfig.register(Config.class, JanksonConfigSerializer::new);
        config = configHolder.getConfig();

        songsFolder = new File(FabricLoader.getInstance().getConfigDir()+File.separator+MOD_ID+File.separator+"songs");
        if (!songsFolder.isDirectory()) songsFolder.mkdirs();

        SongLoader.loadSongs();

        //KeyBinding openScreenKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(MOD_ID+".key_bind.open_screen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.category."+MOD_ID));
        // 修复按键绑定
        KeyMapping openScreenKeyBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                MOD_ID + ".key_bind.open_screen",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.START_CLIENT_TICK.register(new ClientTickEvents.StartTick() {
            private ClientLevel prevWorld;

            @Override
            public void onStartTick(@NonNull Minecraft client) {
                if (prevWorld != client.level) {
                    PREVIEWER.stop();
                    SONG_PLAYER.stop();
                }
                prevWorld = client.level;

                if (openScreenKeyBind.consumeClick()) {
                    if (SongLoader.loadingSongs) {
                        client.gui.getChat().addMessage(Component.translatable(DiscJockey.MOD_ID+".still_loading").withStyle(ChatFormatting.RED));
                        SongLoader.showToast = true;
                    } else {
                        client.setScreen(new DiscJockeyScreen());
                    }
                }
            }
        });

        ClientTickEvents.START_WORLD_TICK.register(world -> {
            for (ClientTickEvents.StartWorldTick listener : TICK_LISTENERS) listener.onStartTick(world);
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            DiscjockeyCommand.register(dispatcher);
        });

        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> {
            PREVIEWER.stop();
            SONG_PLAYER.stop();
        });

        HudRenderCallback.EVENT.register(BlocksOverlay::render);
    }
}
