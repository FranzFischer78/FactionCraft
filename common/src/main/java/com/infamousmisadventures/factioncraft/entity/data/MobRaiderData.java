package com.infamousmisadventures.factioncraft.entity.data;

import com.infamousmisadventures.factioncraft.entity.ai.goal.MoveTowardsRaidGoal;
import com.infamousmisadventures.factioncraft.entity.ai.goal.RaidOpenDoorGoal;
import com.infamousmisadventures.factioncraft.entity.ai.goal.RaiderMoveThroughVillageGoal;
import com.infamousmisadventures.factioncraft.level.data.RaidManager;
import com.infamousmisadventures.factioncraft.mixins.MobAccessor;
import com.infamousmisadventures.factioncraft.raid.Raid;
import com.infamousmisadventures.factioncraft.registry.FCMemoryModuleTypes;
import com.infamousmisadventures.factioncraft.util.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.npc.AbstractVillager;

import java.util.ArrayList;
import java.util.List;

import static com.infamousmisadventures.factioncraft.util.BrainHelper.hasBrain;

public class MobRaiderData implements INBTSerializable<CompoundTag> {

    protected Raid raid;
    private int wave;
    private boolean canJoinRaid = true;
    private int ticksOutsideRaid;
    private boolean waveLeader = false;
    private final Mob entity;
    private final List<Goal> addedGoals = new ArrayList<>();

    public MobRaiderData(Mob entity) {
        this.entity = entity;
    }

    public Raid getRaid() {
        return raid;
    }

    public void setRaid(Raid raid) {
        this.raid = raid;
        updateRaidAI();
    }

    public boolean hasActiveRaid() {
        return raid != null && raid.isActive();
    }

    public int getWave() {
        return wave;
    }

    public void setWave(int wave) {
        this.wave = wave;
    }

    public boolean isCanJoinRaid() {
        return canJoinRaid;
    }

    public void setCanJoinRaid(boolean canJoinRaid) {
        this.canJoinRaid = canJoinRaid;
    }

    public int getTicksOutsideRaid() {
        return ticksOutsideRaid;
    }

    public void setTicksOutsideRaid(int ticksOutsideRaid) {
        this.ticksOutsideRaid = ticksOutsideRaid;
    }

    public void addToRaid(int pWave, Raid raid) {
        setRaid(raid);
        setWave(pWave);
        setCanJoinRaid(true);
        setTicksOutsideRaid(0);
    }

    public boolean isWaveLeader() {
        return waveLeader;
    }

    public void setWaveLeader(boolean waveLeader) {
        this.waveLeader = waveLeader;
    }

    public void updateRaidAI() {
        if (hasBrain(this.entity)) {
            updateRaidBrain();
        } else {
            updateRaidGoals();
        }
    }

    private void updateRaidBrain() {
        if (this.raid != null) {
            this.entity.getBrain().setMemory(FCMemoryModuleTypes.RAID.get(), this.raid);
        } else {
            this.entity.getBrain().eraseMemory(FCMemoryModuleTypes.RAID.get());
        }
    }

    private void updateRaidGoals() {
        if (this.raid != null) {
            if (GoalUtils.hasGroundPathNavigation(this.entity)) {
                addGoal(2, new RaidOpenDoorGoal(entity));
            }
            addGoal(3, new MoveTowardsRaidGoal<>(this.entity));
            addGoal(4, new RaiderMoveThroughVillageGoal(entity, 1.05F, 1, () -> true, 15));
            addGoal(3, new NearestAttackableTargetGoal<>(this.entity, AbstractVillager.class, false));
        } else {
            this.addedGoals.forEach(((MobAccessor) this.entity).getGoalSelector()::removeGoal);
            addedGoals.clear();
        }
    }

    private void addGoal(int priority, Goal goal) {
        ((MobAccessor) this.entity).getGoalSelector().addGoal(priority, goal);
        addedGoals.add(goal);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Wave", this.wave);
        tag.putBoolean("CanJoinRaid", this.canJoinRaid);
        if (this.raid != null) {
            tag.putInt("RaidId", this.raid.getId());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.wave = tag.getInt("Wave");
        this.canJoinRaid = tag.getBoolean("CanJoinRaid");
        if (tag.contains("RaidId", 3)) {
            if (this.entity.level() instanceof ServerLevel level) {
                RaidManager raidManagerCapability = ((IRaidManagerHolder) level).getRaidManager();
                this.setRaid(raidManagerCapability.getRaids().get(tag.getInt("RaidId")));
            }

            if (this.raid != null) {
                this.raid.addWaveMob(this.wave, this.entity, false);
                if (this.waveLeader) {//TODO: How does this know?
                    this.raid.setLeader(this.wave, this.entity);
                }
            }
        }

    }
}
