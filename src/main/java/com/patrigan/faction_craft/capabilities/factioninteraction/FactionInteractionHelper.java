package com.infamousmisadventures.factioncraft.capabilities.factioninteraction;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

import static com.infamousmisadventures.factioncraft.capabilities.ModCapabilities.FACTION_INTERACTION_CAPABILITY;


public class FactionInteractionHelper {

    public static FactionInteraction getFactionInteractionCapability(Player player)
    {
        return player.getCapability(FACTION_INTERACTION_CAPABILITY).orElse(new FactionInteraction());
    }
}
