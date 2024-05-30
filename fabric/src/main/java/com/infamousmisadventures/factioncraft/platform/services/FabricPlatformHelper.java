package com.infamousmisadventures.factioncraft.platform.services;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class FabricPlatformHelper implements IPlatformHelper {

    public MinecraftServer server;

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public MinecraftServer getCurrentServer() {
        return server;
    }

    public void registerServerTickEvent(Runnable runnable) {
        ServerTickEvents.END_SERVER_TICK.register(server -> runnable.run());
    }

    public void registerServer(MinecraftServer server) {
        this.server = server;
    }
}
