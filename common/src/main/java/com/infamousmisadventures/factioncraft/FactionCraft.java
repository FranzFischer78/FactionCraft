package com.infamousmisadventures.factioncraft;

import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.tags.EntityTags;

public class FactionCraft {

    public static void init() {
        Services.REGISTRY_CREATOR.setupRegistryCreator();
        Services.REGISTRAR.setupRegistrar();
        Services.NETWORK_HANDLER.setupNetworkHandler();
        EntityTags.register();
    }
}