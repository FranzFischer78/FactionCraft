package com.infamousmisadventures.factioncraft.datapack;

import com.infamousmisadventures.factioncraft.faction.FactionReloadListener;
import com.infamousmisadventures.factioncraft.registry.FCBoosts;
import com.infamousmisadventures.factioncraft.registry.FCFactionEntityTypes;
import com.infamousmisadventures.factioncraft.registry.FCFactions;
import com.infamousmisadventures.factioncraft.registry.FCRaidConfigTypes;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.List;

public class DatapackReloadListener {

    public static List<PreparableReloadListener> reloadListeners() {
        return List.of(
                FCBoosts.BOOSTS,
                FCFactions.FACTION_DATA,
                FCFactionEntityTypes.FACTION_ENTITY_TYPE_DATA,
                FCRaidConfigTypes.RAID_CONFIG_TYPES,
                new FactionReloadListener()
        );
    }
}
