package com.infamousmisadventures.factioncraft;

import com.infamousmisadventures.factioncraft.datapack.DatapackReloadListener;
import Services;
import net.minecraftforge.fml.common.Mod;

@Mod(FCConstants.MOD_ID)
public class FactionCraftForge {
    
    public FactionCraftForge() {
        FactionCraft.init();
        setupDatapackFormats();
        setupEvents();
    }

    private void setupDatapackFormats() {
    }

    public void setupEvents() {
        MinecraftForge.EVENT_BUS.addListener(DatapackReloadListener::onAddReloadListeners);
    }
}