package com.infamousmisadventures.factioncraft;

import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.platform.services.FabricPlatformHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FactionCraftFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        FactionCraft.init();
        setupDatapackFormats();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ((FabricPlatformHelper) Services.PLATFORM).registerServer(server);
        });
    }

    private void setupDatapackFormats() {
    }

}
