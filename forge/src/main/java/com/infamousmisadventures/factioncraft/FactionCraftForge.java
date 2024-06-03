package com.infamousmisadventures.factioncraft;

import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.datapack.DatapackReloadListener;
import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.platform.services.ForgeRegistrar;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FCConstants.MOD_ID)
public class FactionCraftForge {
    
    public FactionCraftForge() {
        FactionCraft.init();
        setupDatapackFormats();
        setupEvents();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ForgeRegistrar.getCachedRegistries().values().forEach((registry) -> {
            registry.register(modBus);
        });
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FactionCraftConfig.COMMON_SPEC);
    }

    private void setupDatapackFormats() {
    }

    public void setupEvents() {
        MinecraftForge.EVENT_BUS.addListener(DatapackReloadListener::onAddReloadListeners);
    }
}