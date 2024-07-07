package com.infamousmisadventures.factioncraft.level.spawner;

import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.level.saveddata.RaidManager;
import com.infamousmisadventures.factioncraft.raid.config.raid.FactionBattleConfig;
import com.infamousmisadventures.factioncraft.raid.config.raid.FactionBattleConfigType;
import com.infamousmisadventures.factioncraft.raid.config.raid.RaidConfigType;
import com.infamousmisadventures.factioncraft.registry.FCFactions;
import com.infamousmisadventures.factioncraft.registry.FCRaidConfigTypes;
import com.infamousmisadventures.factioncraft.util.GeneralUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.stream.Collectors;

public class BattleSpawner implements CustomSpawner {
    private int nextTick;

    public int tick(ServerLevel pLevel, boolean pSpawnHostiles, boolean pSpawnPassives) {
        if (!pSpawnHostiles) {
            return 0;
        } else if (FactionCraftConfig.DISABLE_FACTION_BATTLES.get()) {
            return 0;
        } else {
            RandomSource random = pLevel.random;
            --this.nextTick;
            if (this.nextTick > 0) {
                return 0;
            } else {
                this.nextTick += FactionCraftConfig.BATTLE_TICK_DELAY_BETWEEN_SPAWN_ATTEMPTS.get() + random.nextInt(FactionCraftConfig.BATTLE_VARIABLE_TICK_DELAY_BETWEEN_SPAWN_ATTEMPTS.get());
                if (pLevel.getDayTime() >= FactionCraftConfig.BATTLE_DAYTIME_BEFORE_SPAWNING.get() && pLevel.isDay()) {
                    if (random.nextFloat() <= FactionCraftConfig.BATTLE_SPAWN_CHANCE_ON_SPAWN_ATTEMPT.get()) {
                        return 0;
                    } else {
                        int j = pLevel.players().size();
                        if (j < 1) {
                            return 0;
                        } else {
                            Player playerentity = pLevel.players().get(random.nextInt(j));
                            if (playerentity.isSpectator()) {
                                return 0;
                            } else if (pLevel.isCloseToVillage(playerentity.blockPosition(), 2)) {
                                return 0;
                            } else {
                                int k = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                                int l = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                                BlockPos.MutableBlockPos blockpos$mutable = playerentity.blockPosition().mutable().move(k, 0, l);
                                if (!pLevel.hasChunksAt(blockpos$mutable.getX() - 10, blockpos$mutable.getY() - 10, blockpos$mutable.getZ() - 10, blockpos$mutable.getX() + 10, blockpos$mutable.getY() + 10, blockpos$mutable.getZ() + 10)) {
                                    return 0;
                                } else {
                                    Holder<Biome> holder = pLevel.getBiome(blockpos$mutable);
                                    if (holder.is(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
                                        return 0;
                                    } else {
                                        return spawnFactionBattle(pLevel, random, blockpos$mutable);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    return 0;
                }
            }
        }
    }

    public static int spawnFactionBattle(ServerLevel pLevel, RandomSource random, BlockPos blockPos) {
        Faction faction1 = FCFactions.getRandomFactionWithEnemies(pLevel, random, faction -> faction.canRaid());
        if (faction1 == null) {
            return 0;
        }
        List<Faction> enemies = faction1.getRelations().getEnemies().stream()
                .map(FCFactions::getFaction)
                .filter(faction -> faction.isActive(pLevel))
                .filter(faction -> faction.isEnemyOf(faction1))
                .collect(Collectors.toList());
        if (enemies.isEmpty()) {
            return 0;
        } else {
            Faction faction2 = GeneralUtils.getRandomItem(enemies, random);
            RaidConfigType type = FCRaidConfigTypes.getRaidConfig(FCRaidConfigTypes.FACTION_BATTLE_CONFIG);
            if (type instanceof FactionBattleConfigType factionBattleConfigType) {
                FactionBattleConfig battleConfig = (FactionBattleConfig) factionBattleConfigType.create();
                battleConfig.init(blockPos, faction1, faction2, pLevel);
                RaidManager raidManager = RaidManager.getOrCreate(pLevel);
                raidManager.createRaid(battleConfig);
            }
            return 1;
        }
    }

}
