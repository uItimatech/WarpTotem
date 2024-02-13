package net.ultimatech.warptotem;

import net.fabricmc.api.ModInitializer;

import net.ultimatech.warptotem.block.WTBlocks;
import net.ultimatech.warptotem.block.entity.WTBlockEntities;
import net.ultimatech.warptotem.item.WTItems;
import net.ultimatech.warptotem.sound.WTSounds;
import net.ultimatech.warptotem.tab.WTVanillaCreativeTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarpTotem implements ModInitializer {

	public static final String MOD_ID = "warptotem";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		WTBlocks.registerModBlocks();
		WTItems.registerModItems();
		WTBlockEntities.registerBlockEntities();
		WTSounds.registerSounds();
		WTVanillaCreativeTab.registerToCreativeTabs();

		LOGGER.info("Warp Totem mod initialized.");
	}
}