package com.infamousmisadventures.factioncraft.platform;

import com.infamousmisadventures.factioncraft.FCConstants;
import com.infamousmisadventures.factioncraft.platform.services.INetworkHandler;
import com.infamousmisadventures.factioncraft.platform.services.IPlatformHelper;
import com.infamousmisadventures.factioncraft.platform.services.IRegistrar;
import com.infamousmisadventures.factioncraft.platform.services.IRegistryCreator;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IRegistrar REGISTRAR = load(IRegistrar.class);
    public static final IRegistryCreator REGISTRY_CREATOR = load(IRegistryCreator.class);
    public static final INetworkHandler NETWORK_HANDLER = load(INetworkHandler.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        FCConstants.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);

        return loadedService;
    }
}