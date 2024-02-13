package net.ultimatech.warptotem.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.ultimatech.warptotem.WarpTotem;
import net.ultimatech.warptotem.block.WTBlocks;

public class WTBlockEntities {

    // ----- BLOCK ENTITIES ----- //

    public static final BlockEntityType<WarpTotemBlockEntity> WARP_TOTEM_BLOCK_ENTITIES =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(WarpTotem.MOD_ID, "warp_totem"),
                    FabricBlockEntityTypeBuilder.create(WarpTotemBlockEntity::new,
                            WTBlocks.WARP_TOTEM).build());




    // ----- REGISTRATION ----- //
    public static void registerBlockEntities() {
        WarpTotem.LOGGER.info("Registering mod blockEntities for " + WarpTotem.MOD_ID);
    }
}
