package net.ultimatech.warptotem.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.ultimatech.warptotem.WarpTotem;

public class WTSounds {

    // --- SOUNDS --- //

    // Event sounds

    public static final SoundEvent WARP_TOTEM_ENABLE = registerSoundEvent("warp_totem_enable");



    // ----- REGISTRATION ----- //
    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(WarpTotem.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
    public static void registerSounds() {
        WarpTotem.LOGGER.info("Registering sounds for " + WarpTotem.MOD_ID);
    }
}
