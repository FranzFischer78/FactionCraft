package com.infamousmisadventures.factioncraft;

import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.registry.*;
import com.infamousmisadventures.factioncraft.tags.EntityTags;

public class FactionCraft {

    public static void init() {
        Services.REGISTRY_CREATOR.setupRegistryCreator();
        Services.REGISTRAR.setupRegistrar();
        runRegistrars();
        Services.NETWORK_HANDLER.setupNetworkHandler();
        EntityTags.register();
    }

    private static void runRegistrars() {
        FCRegistries.register();
        FCActivities.register();
        FCArgumentTypes.register();
        FCBlockEntityTypes.register();
        FCBlocks.register();
        FCMemoryModuleTypes.register();
        FCMobEffects.register();
        FCSensorTypes.register();
        FCItems.register();
        FCBoostTypes.register();
    }
}