package com.infamousmisadventures.factioncraft.entity.ai.brain.task.villager;

import com.google.common.collect.ImmutableMap;
import com.infamousmisadventures.factioncraft.level.saveddata.RaidManager;
import com.infamousmisadventures.factioncraft.raid.Raid;
import com.infamousmisadventures.factioncraft.registry.FCActivities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;

public class BeginVillagerRaidTask extends Behavior<LivingEntity> {
    public BeginVillagerRaidTask() {
        super(ImmutableMap.of());
    }

    protected boolean checkExtraStartConditions(ServerLevel pLevel, LivingEntity pOwner) {
        return pLevel.random.nextInt(
                20) == 0;
    }

    protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        RaidManager raidManagerCapability = RaidManager.getOrCreate(level);
        Raid raid = raidManagerCapability.getRaidAt(entity.blockPosition());
        if (raid != null) {
            if (raid.hasFirstWaveSpawned() && !raid.isBetweenWaves()) {
                brain.setDefaultActivity(FCActivities.FACTION_RAID.get());
                brain.setActiveActivityIfPossible(FCActivities.FACTION_RAID.get());
            } else {
                brain.setDefaultActivity(FCActivities.PRE_FACTION_RAID.get());
                brain.setActiveActivityIfPossible(FCActivities.PRE_FACTION_RAID.get());
            }
        }

    }
}