package com.infamousmisadventures.factioncraft.entity.data;


import com.infamousmisadventures.factioncraft.boost.Boost;
import com.infamousmisadventures.factioncraft.util.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppliedBoostsData implements INBTSerializable<CompoundTag> {

    List<Boost> appliedBoosts = new ArrayList<>();

    public List<Boost> getAppliedBoosts() {
        return appliedBoosts;
    }

    public void addAppliedBoost(Boost appliedBoost) {
        appliedBoosts.add(appliedBoost);
    }

    public void setAppliedBoosts(List<Boost> appliedBoosts) {
        this.appliedBoosts = appliedBoosts;
    }

    public List<Boost> getBoostsOfType(Boost.BoostGroup boostGroup) {
        return appliedBoosts.stream().filter(boost -> boost.getBoostGroup().equals(boostGroup)).collect(Collectors.toList());
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        list.addAll(this.getAppliedBoosts().stream().map(boost -> boost.save(new CompoundTag())).toList());
        tag.put("appliedBoosts", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ListTag appliedBoostsList = tag.getList("appliedBoosts", 10);
        this.setAppliedBoosts(appliedBoostsList.stream().map(inbt -> Boost.load((CompoundTag) inbt)).collect(Collectors.toList()));
    }

    public void onEntityJoin(LivingEntity entity) {
        if(!entity.level().isClientSide() && entity instanceof Mob mob) {
            getAppliedBoosts().forEach(boost -> boost.applyAIChanges(mob));
        }
    }
}
