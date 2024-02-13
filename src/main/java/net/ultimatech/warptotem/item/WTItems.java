package net.ultimatech.warptotem.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.ultimatech.warptotem.WTDependencyManager;
import net.ultimatech.warptotem.WarpTotem;
import net.ultimatech.warptotem.block.WTBlocks;

public class WTItems {

    // ----- ITEMS ----- //
    public static final Item WARP_TOTEM = WTDependencyManager.isEchoingWildsInstalled()? null :  registerItem("warp_totem", new AliasedBlockItem(WTBlocks.WARP_TOTEM, new FabricItemSettings()));


    // ----- REGISTRATION ----- //
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM,new Identifier(WarpTotem.MOD_ID, name), item);
    }

    public static void registerModItems() {
        WarpTotem.LOGGER.info("Registering mod items for " + WarpTotem.MOD_ID);
    }
}
