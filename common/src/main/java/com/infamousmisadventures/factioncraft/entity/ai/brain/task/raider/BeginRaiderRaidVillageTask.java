package com.infamousmisadventures.factioncraft.entity.ai.brain.task.raider;

import com.google.common.collect.ImmutableMap;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import com.infamousmisadventures.factioncraft.raid.Raid;
import com.infamousmisadventures.factioncraft.raid.target.RaidTarget;
import com.infamousmisadventures.factioncraft.registry.FCActivities;
import com.infamousmisadventures.factioncraft.registry.FCMemoryModuleTypes;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;

import java.util.ArrayList;
import java.util.List;

public class BeginRaiderRaidVillageTask extends Behavior<LivingEntity> {
    public BeginRaiderRaidVillageTask() {
        super(ImmutableMap.of());
    }

    protected boolean checkExtraStartConditions(ServerLevel pLevel, LivingEntity entity) {
        if (pLevel.random.nextInt(20) == 0 && entity instanceof Mob mob) {
            Raid raid = ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData().getRaid();
            if (raid != null) {
                return raid.getRaidTarget().getRaidType() == RaidTarget.Type.VILLAGE && pLevel.isVillage(entity.blockPosition());
            }
        }
        return false;
    }

    protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
        if (entity instanceof Mob mob) {
            Brain<?> brain = entity.getBrain();
            Raid raid = ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData().getRaid();
            if (raid != null) {
                if(raid.getRaidTarget().getRaidType() == RaidTarget.Type.VILLAGE) {
                    brain.eraseMemory(FCMemoryModuleTypes.RAID_WALK_TARGET.get());
                    brain.setMemory(FCMemoryModuleTypes.RAIDED_VILLAGE_POI.get(), new ArrayList<>(List.of(GlobalPos.of(level.dimension(), raid.getRaidTarget().getTargetBlockPos()))));
                    brain.setDefaultActivity(FCActivities.FACTION_RAIDER_VILLAGE.get());
                    brain.setActiveActivityIfPossible(FCActivities.FACTION_RAIDER_VILLAGE.get());
                }
            }
        }
    }
}
