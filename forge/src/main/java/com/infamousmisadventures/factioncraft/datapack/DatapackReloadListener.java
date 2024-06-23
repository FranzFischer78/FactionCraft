package com.infamousmisadventures.factioncraft.datapack;

import com.infamousmisadventures.factioncraft.faction.FactionReloadListener;
import com.infamousmisadventures.factioncraft.registry.FCBoosts;
import com.infamousmisadventures.factioncraft.registry.FCRaidConfigTypes;
import net.minecraftforge.event.AddReloadListenerEvent;

import static com.infamousmisadventures.factioncraft.registry.FCFactionEntityTypes.FACTION_ENTITY_TYPE_DATA;
import static com.infamousmisadventures.factioncraft.registry.FCFactions.FACTION_DATA;

public class DatapackReloadListener{

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(FCBoosts.BOOSTS);
        event.addListener(FACTION_DATA);
        event.addListener(FACTION_ENTITY_TYPE_DATA);
        event.addListener(FCRaidConfigTypes.RAID_CONFIG_TYPES);
        event.addListener(new FactionReloadListener());
    }
}
