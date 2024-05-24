package com.infamousmisadventures.factioncraft.capabilities.dominion;

import net.minecraft.world.level.Level;

import static com.infamousmisadventures.factioncraft.capabilities.ModCapabilities.DOMINION_CAPABILITY;


public class DominionHelper {

    public static Dominion getCapability(Level level)
    {
        return level.getCapability(DOMINION_CAPABILITY).orElse(new Dominion());
    }
}
