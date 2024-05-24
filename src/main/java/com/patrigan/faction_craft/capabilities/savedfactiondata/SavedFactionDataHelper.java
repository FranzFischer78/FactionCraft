package com.infamousmisadventures.factioncraft.capabilities.savedfactiondata;

import net.minecraft.world.level.Level;

import static com.infamousmisadventures.factioncraft.capabilities.ModCapabilities.SAVED_FACTION_DATA_CAPABILITY;


public class SavedFactionDataHelper {

    public static SavedFactionData getCapability(Level level)
    {
        return level.getCapability(SAVED_FACTION_DATA_CAPABILITY).orElse(new SavedFactionData());
    }
}
