package com.infamousmisadventures.factioncraft.entity.ai.brain.task.raider;

import com.google.common.collect.ImmutableMap;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import com.infamousmisadventures.factioncraft.raid.Raid;
import com.infamousmisadventures.factioncraft.registry.FCActivities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;

public class BeginRaiderRaidPrepTask extends Behavior<LivingEntity> {
    public BeginRaiderRaidPrepTask() {
        super(ImmutableMap.of());
    }

    protected boolean checkExtraStartConditions(ServerLevel pLevel, LivingEntity entity) {
        if (entity instanceof Mob mob) {
            return ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData().hasActiveRaid() && (!entity.getBrain().isActive(FCActivities.FACTION_RAIDER_PREP.get()) || !entity.getBrain().isActive(FCActivities.FACTION_RAIDER_VILLAGE.get()));
        }
        return false;
    }

    protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
        if (entity instanceof Mob mob) {
            Brain<?> brain = entity.getBrain();
            Raid raid = ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData().getRaid();
            if (raid != null) {
                brain.setDefaultActivity(FCActivities.FACTION_RAIDER_PREP.get());
                brain.setActiveActivityIfPossible(FCActivities.FACTION_RAIDER_PREP.get());
            }
        }
    }
}
