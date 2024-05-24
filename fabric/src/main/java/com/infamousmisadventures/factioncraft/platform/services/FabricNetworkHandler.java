package com.infamousmisadventures.factioncraft.platform.services;

import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.resources.ResourceLocation;

import static com.infamousmisadventures.factioncraft.FCConstants.MOD_ID;

public class FabricNetworkHandler implements INetworkHandler {
    public static final SimpleChannel INSTANCE = new SimpleChannel(new ResourceLocation(MOD_ID, "network"));

    protected static int PACKET_COUNTER = 0;

    public FabricNetworkHandler() {
    }

    @Override
    public void setupNetworkHandler() {
        // Server to Client
    }

    public static int incrementAndGetPacketCounter() {
        return PACKET_COUNTER++;
    }
}
