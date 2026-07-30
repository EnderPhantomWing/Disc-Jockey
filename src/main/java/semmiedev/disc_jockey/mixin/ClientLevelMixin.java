package semmiedev.disc_jockey.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import semmiedev.disc_jockey.DiscJockey;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "playSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZJ)V", at = @At("HEAD"), cancellable = true)
    private void makeNoteBlockSoundsOmnidirectional(double x, double y, double z, SoundEvent event, SoundSource category, float volume, float pitch, boolean useDistance, long seed, CallbackInfo ci) {
        if (((DiscJockey.config.omnidirectionalNoteBlockSounds && DiscJockey.SONG_PLAYER.running) || DiscJockey.PREVIEWER.running) && event.location().getPath().startsWith("block.note_block")) {
            ci.cancel();
            minecraft.getSoundManager().play(new SimpleSoundInstance(event.location(), category, volume, pitch, RandomSource.create(seed), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true));
        }
    }
}
