package com.infamousmisadventures.factioncraft.datapack;

import com.infamousmisadventures.factioncraft.faction.FactionReloadListener;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.List;

import static com.infamousmisadventures.factioncraft.registry.FCFactionEntityTypes.FACTION_ENTITY_TYPE_DATA;
import static com.infamousmisadventures.factioncraft.registry.FCFactions.FACTION_DATA;

public class DatapackReloadListener {

    public static List<PreparableReloadListener> reloadListeners() {
        return List.of(
                FACTION_DATA,
                FACTION_ENTITY_TYPE_DATA,
                new FactionReloadListener()
        );
    }
}
