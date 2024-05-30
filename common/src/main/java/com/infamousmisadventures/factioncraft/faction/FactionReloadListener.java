package com.infamousmisadventures.factioncraft.faction;

import com.infamousmisadventures.factioncraft.FactionCraft;
import com.infamousmisadventures.factioncraft.faction.entity.FactionEntityType;
import com.infamousmisadventures.factioncraft.level.saveddata.FactionRelationsData;
import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.registry.FCFactionEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

import static com.infamousmisadventures.factioncraft.faction.relations.FactionRelation.ALLY_MAX;
import static com.infamousmisadventures.factioncraft.faction.relations.FactionRelation.ENEMY_MAX;
import static com.infamousmisadventures.factioncraft.registry.FCFactionEntityTypes.FACTION_ENTITY_TYPE_DATA;
import static com.infamousmisadventures.factioncraft.registry.FCFactions.FACTION_DATA;
import static com.infamousmisadventures.factioncraft.registry.FCFactions.reloadPlayerFactions;

@Mod.EventBusSubscriber(modid = FactionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FactionReloadListener implements ResourceManagerReloadListener {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event)
    {
        event.addListener(FACTION_DATA);
        event.addListener(FACTION_ENTITY_TYPE_DATA);
        event.addListener(new FactionReloadListener());
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        FACTION_DATA.getData().forEach((key, faction) -> {
            addEntities(faction);
            updateInitialRelationships(faction);
            updateActualRelationships(faction);
        });
        reloadPlayerFactions();
    }

    private void updateActualRelationships(Faction faction) {
        MinecraftServer currentServer = Services.PLATFORM.getCurrentServer();
        if(currentServer == null) return;
        FactionRelationsData savedFactionData = FactionRelationsData.getOrCreate(currentServer.overworld());
        faction.getRelations().initiateActualRelations(savedFactionData.getOriginalRelations(faction));
    }

    private void updateInitialRelationships(Faction faction) {
        faction.getRelations().getEnemies().forEach(enemy -> {
            FACTION_DATA.getData().get(enemy).getRelations().setInitialRelation(faction, ENEMY_MAX);
        });
        faction.getRelations().getAllies().forEach(ally -> {
            FACTION_DATA.getData().get(ally).getRelations().setInitialRelation(faction, ALLY_MAX);
        });
    }

    private void addEntities(Faction faction) {
        Map<ResourceLocation, FactionEntityType> factionEntityTypeData = FCFactionEntityTypes.getFactionEntityTypeData(faction);
        faction.addEntityTypes(factionEntityTypeData.values());
    }
}
