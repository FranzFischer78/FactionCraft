package com.infamousmisadventures.factioncraft;

import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.tags.EntityTags;

public class FactionCraft {

    public static void init() {
        Services.REGISTRAR.setupRegistrar();
        Services.REGISTRY_CREATOR.setupRegistryCreator();
        Services.NETWORK_HANDLER.setupNetworkHandler();
        EntityTags.register();
    }
}