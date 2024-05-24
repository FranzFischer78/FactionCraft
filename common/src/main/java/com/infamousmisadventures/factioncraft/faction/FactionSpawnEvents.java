package com.infamousmisadventures.factioncraft.faction;

import com.infamousmisadventures.factioncraft.FactionCraft;
import com.infamousmisadventures.factioncraft.capabilities.dominion.AreaDominion;
import com.infamousmisadventures.factioncraft.capabilities.dominion.AreaPos;
import com.infamousmisadventures.factioncraft.capabilities.dominion.Dominion;
import com.infamousmisadventures.factioncraft.capabilities.dominion.DominionHelper;
import com.infamousmisadventures.factioncraft.capabilities.factionentity.FactionEntity;
import com.infamousmisadventures.factioncraft.capabilities.factionentity.FactionEntityHelper;
import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.registry.FCFactions;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.infamousmisadventures.factioncraft.config.FactionCraftConfig.DOMINION_FACTION_SPAWN_TRESHOLD;
import static com.infamousmisadventures.factioncraft.config.FactionCraftConfig.DOMINION_SUPPRESS_GAIA_SPAWN_TRESHOLD;
import static com.infamousmisadventures.factioncraft.faction.Faction.GAIA;

@Mod.EventBusSubscriber(modid = FactionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FactionSpawnEvents {

    @SubscribeEvent
    public static void addDominionSpawns(LevelEvent.PotentialSpawns event) {
        if (FactionCraftConfig.ENABLE_DOMINION.get() && event.getLevel() instanceof Level level) {
            if(event.getMobCategory() != MobCategory.MONSTER) return;
            Dominion dominion = DominionHelper.getCapability(level);
            AreaDominion chunkDominion = dominion.getAreaDominion(level, new AreaPos(event.getPos()));
            chunkDominion.getFactionDominions().entrySet().stream().filter(entry -> !entry.getKey().equals(GAIA.getName())).filter(entry -> entry.getValue() > DOMINION_SUPPRESS_GAIA_SPAWN_TRESHOLD.get()).findFirst().ifPresent(dominionAmount -> {
                List<MobSpawnSettings.SpawnerData> spawnerDataList = new ArrayList<>(event.getSpawnerDataList());
                spawnerDataList.forEach(event::removeSpawnerData);
            });
            getSpawningFactionsStream(chunkDominion).forEach(faction -> {
                int dominionAmount = chunkDominion.getFactionDominion(faction);
                faction.getDominionSpawners(event.getLevel(), event.getPos(), dominionAmount).forEach(event::addSpawnerData);
            });
        }
    }

    private static Stream<Faction> getSpawningFactionsStream(AreaDominion areaDominion) {
        return areaDominion.getFactionDominions().entrySet().stream()
                .filter(entry -> entry.getValue() > DOMINION_FACTION_SPAWN_TRESHOLD.get())
                .map(entry -> FCFactions.getFaction(entry.getKey()));
    }

    @SubscribeEvent
    public static void onEntityJoinLevelEvent(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk() || !FactionCraftConfig.ENABLE_DOMINION.get()) return;
        if (event.getEntity() instanceof net.minecraft.world.entity.Mob mob) {
            FactionEntity factionEntity = FactionEntityHelper.getFactionEntityCapability(mob);
            if (factionEntity.getFaction() == null || factionEntity.getFaction() == GAIA) {
                Dominion dominion = DominionHelper.getCapability(event.getLevel());
                AreaDominion areaDominion = dominion.getAreaDominion(event.getLevel(), new AreaPos(event.getEntity().blockPosition()));
                List<WeightedEntry.Wrapper<Consumer<Entity>>> factionEntityConverters = getSpawningFactionsStream(areaDominion)
                        .flatMap(faction -> getWeightedEntries(faction, event.getLevel(), event.getEntity().blockPosition(), areaDominion.getFactionDominion(faction)).stream())
                        .toList();
                WeightedRandom.getRandomItem(event.getLevel().getRandom(), factionEntityConverters).ifPresent(wrapper -> {
                    wrapper.getData().accept(event.getEntity());
                });
            }
        }
    }

    private static List<WeightedEntry.Wrapper<Consumer<Entity>>> getWeightedEntries(Faction faction, Level level, BlockPos pos, int dominionAmount) {
        return faction.getSpawnableFactionEntityTypes(level, pos, dominionAmount).stream()
                .map(factionEntityType -> WeightedEntry.wrap(((Consumer<Entity>) (Entity entity) -> factionEntityType.convertEntity(faction, entity)), factionEntityType.getSpawnWeight()))
                .toList();
    }

}
