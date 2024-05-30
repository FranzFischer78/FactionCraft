package com.infamousmisadventures.factioncraft.entity.data;


import com.infamousmisadventures.factioncraft.entity.ai.goal.PatrolGoal;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.mixins.MobAccessor;
import com.infamousmisadventures.factioncraft.registry.FCMemoryModuleTypes;
import com.infamousmisadventures.factioncraft.util.INBTSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

import static com.infamousmisadventures.factioncraft.util.BrainHelper.hasBrain;

public class MobPatrollerData implements INBTSerializable<CompoundTag> {

    private BlockPos patrolTarget = null;
    private boolean patrolLeader = false;
    private boolean patrolling = false;
    private final Mob entity;
    private Goal goal;

    public MobPatrollerData(Mob entity) {
        this.entity = entity;
        this.goal = new PatrolGoal<>(this.entity, 0.7D, 0.595D);
    }

    public BlockPos getPatrolTarget() {
        return patrolTarget;
    }

    public void setPatrolTarget(BlockPos patrolTarget) {
        this.patrolTarget = patrolTarget;
    }

    public boolean isPatrolLeader() {
        return patrolLeader;
    }

    public void setPatrolLeader(boolean patrolLeader) {
        this.patrolLeader = patrolLeader;
    }

    public boolean isPatrolling() {
        return patrolling;
    }

    public void setPatrolling(boolean patrolling) {
        this.patrolling = patrolling;
        updatePatrolAI();
    }

    public boolean hasPatrolTarget() {
        return patrolTarget != null;
    }

    public void findPatrolTarget() {
        this.patrolTarget = this.entity.blockPosition().offset(-500 + this.entity.getRandom().nextInt(1000), 0, -500 + this.entity.getRandom().nextInt(1000));
        this.patrolling = true;
    }

    public boolean canJoinPatrol(Mob mob) {
        FactionEntityData thisCap = ((IFactionEntityDataHolder) this.entity).getOrCreateFactionEntityData();
        FactionEntityData otherCap = ((IFactionEntityDataHolder) mob).getOrCreateFactionEntityData();
        return thisCap.getFaction() != null && thisCap.getFaction().equals(otherCap.getFaction());
    }

    public float getPatrollerWalkSpeed(Mob mobEntity)
    {
        return isPatrolLeader() ? 0.595F : 0.7F;
    }

    public CompoundTag save(CompoundTag compoundNbt) {
        if (this.patrolTarget != null) {
            compoundNbt.put("PatrolTarget", NbtUtils.writeBlockPos(this.patrolTarget));
        }
        compoundNbt.putBoolean("PatrolLeader", this.patrolLeader);
        compoundNbt.putBoolean("Patrolling", this.patrolling);
        return compoundNbt;
    }

    public void load(CompoundTag compoundNbt) {
        if (compoundNbt.contains("PatrolTarget")) {
            this.patrolTarget = NbtUtils.readBlockPos(compoundNbt.getCompound("PatrolTarget"));
        }

        this.patrolLeader = compoundNbt.getBoolean("PatrolLeader");
        this.patrolling = compoundNbt.getBoolean("Patrolling");
        updatePatrolGoals();
    }

    private void updatePatrolAI() {
        if (hasBrain(this.entity)) {
            updatePatrolBrain();
        } else {
            updatePatrolGoals();
        }
    }

    private void updatePatrolBrain() {
        this.entity.getBrain().setMemory(FCMemoryModuleTypes.PATROLLER.get(), this.patrolling);
    }

    private void updatePatrolGoals() {
        if (this.isPatrolling()) {
            ((MobAccessor) this.entity).getGoalSelector().addGoal(4, goal);
        } else {
            ((MobAccessor) this.entity).getGoalSelector().removeGoal(goal);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundNbt = new CompoundTag();
        if (this.patrolTarget != null) {
            compoundNbt.put("PatrolTarget", NbtUtils.writeBlockPos(this.patrolTarget));
        }
        compoundNbt.putBoolean("PatrolLeader", this.patrolLeader);
        compoundNbt.putBoolean("Patrolling", this.patrolling);
        return compoundNbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("PatrolTarget")) {
            this.patrolTarget = NbtUtils.readBlockPos(tag.getCompound("PatrolTarget"));
        }

        this.patrolLeader = tag.getBoolean("PatrolLeader");
        this.patrolling = tag.getBoolean("Patrolling");
        updatePatrolGoals();
    }

    public void onEntityJoin(){
        if (isPatrolling()) {
            if (entity instanceof AbstractPiglin piglin) {
                piglin.setImmuneToZombification(true);
            }
        }
    }

    public void onEntityDie(){
    }
}
