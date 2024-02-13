package net.ultimatech.warptotem.tab;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.ultimatech.warptotem.WTDependencyManager;
import net.ultimatech.warptotem.WarpTotem;
import net.ultimatech.warptotem.item.WTItems;

public class WTVanillaCreativeTab {

    private static void addToTab_REDSTONE(FabricItemGroupEntries entries) {
        entries.add(WTItems.WARP_TOTEM);
    }

    public static void registerToCreativeTabs() {
        WarpTotem.LOGGER.info("Registering items to vanilla item groups for " + WarpTotem.MOD_ID);

        if (!WTDependencyManager.isEchoingWildsInstalled()) ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(WTVanillaCreativeTab::addToTab_REDSTONE);
    }
}
