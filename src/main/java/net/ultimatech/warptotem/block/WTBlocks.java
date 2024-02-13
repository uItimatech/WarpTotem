package net.ultimatech.warptotem.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.ultimatech.warptotem.WarpTotem;

public class WTBlocks {
    public static final Block WARP_TOTEM = registerBlockWithoutItem("warp_totem",
            new WarpTotemBlock(FabricBlockSettings.copy(Blocks.MUD_BRICKS).nonOpaque().blockVision(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never)));


    // ----- REGISTRATION ----- //
    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(WarpTotem.MOD_ID, name), block);
    }

    private static Block registerBlockWithoutItem(String name, Block block) {
        return Registry.register(Registries.BLOCK, new Identifier(WarpTotem.MOD_ID, name), block);
    }

    @SuppressWarnings("unused")
    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(WarpTotem.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        WarpTotem.LOGGER.info("Registering mod blocks for " + WarpTotem.MOD_ID);
    }
}
