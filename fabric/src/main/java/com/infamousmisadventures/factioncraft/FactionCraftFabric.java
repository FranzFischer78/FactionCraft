package com.infamousmisadventures.factioncraft;

import net.fabricmc.api.ModInitializer;

public class FactionCraftFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        FactionCraft.init();
        setupDatapackFormats();
    }

    private void setupDatapackFormats() {
    }
}
