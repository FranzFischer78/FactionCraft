package com.infamousmisadventures.factioncraft;

import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.platform.services.FabricPlatformHelper;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraftforge.fml.config.ModConfig;

import static com.infamousmisadventures.factioncraft.FCConstants.MOD_ID;

public class FactionCraftFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        FactionCraft.init();
        ForgeConfigRegistry.INSTANCE.register(MOD_ID,ModConfig.Type.COMMON, FactionCraftConfig.COMMON_SPEC);
        setupDatapackFormats();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ((FabricPlatformHelper) Services.PLATFORM).registerServer(server);
        });
    }

    private void setupDatapackFormats() {
    }

}
