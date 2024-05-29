package com.infamousmisadventures.factioncraft;

import com.infamousmisadventures.factioncraft.registry.ModBlocks;
import com.infamousmisadventures.factioncraft.registry.ModBlockEntityTypes;
import com.infamousmisadventures.factioncraft.capabilities.ModCapabilities;
import com.infamousmisadventures.factioncraft.commands.arguments.ModArgumentTypes;
import com.infamousmisadventures.factioncraft.compat.GuardVillagerCompat;
import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.effect.ModMobEffects;
import com.infamousmisadventures.factioncraft.registry.FCActivities;
import com.infamousmisadventures.factioncraft.network.NetworkHandler;
import com.infamousmisadventures.factioncraft.registry.FCMemoryModuleTypes;
import com.infamousmisadventures.factioncraft.registry.FCSensorTypes;
import com.infamousmisadventures.factioncraft.tags.EntityTags;
import Services;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("faction_craft")
public class FactionCraft
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "faction_craft";

    public FactionCraft() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FactionCraftConfig.COMMON_SPEC);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
        // Register the doClientStuff method for modloading

        //Register Custom Tags
        EntityTags.register();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        FCSensorTypes.SENSOR_TYPES.register(modEventBus);
        FCMemoryModuleTypes.MEMORY_MODULE_TYPES.register(modEventBus);
        FCActivities.ACTIVITIES.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModMobEffects.MOB_EFFECTS.register(modEventBus);
        ModArgumentTypes.COMMAND_ARGUMENT_TYPES.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);

        registerCompatEvents();

        ModCapabilities.setupCapabilities();
    }

    private void registerCompatEvents() {
        GuardVillagerCompat.registerEventHandlers();
    }

    private void setup(final FMLCommonSetupEvent event){
        event.enqueueWork(NetworkHandler::init);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ModBlocks.initRenderTypes();
    }

}
