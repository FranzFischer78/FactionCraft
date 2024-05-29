package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.boost.Boost;
import com.infamousmisadventures.factioncraft.boost.NoBoost;
import com.infamousmisadventures.factioncraft.entity.data.AppliedBoostsData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IAppliedBoostsDataHolder;
import com.infamousmisadventures.factioncraft.util.GeneralUtils;
import com.infamousmisadventures.factioncraft.util.data.CodecJsonDataManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCBoosts {
    public static final ResourceLocation RESOURCELOCATION = modLoc("faction_entity_type");

    public static final CodecJsonDataManager<Boost> BOOSTS = new CodecJsonDataManager<>(RESOURCELOCATION, "boost", Boost.CODEC);


    public static Boost getBoost(ResourceLocation factionResourceLocation){
        return BOOSTS.getData().getOrDefault(factionResourceLocation, NoBoost.INSTANCE);
    }

    public static boolean boostExists(ResourceLocation boostResourceLocation){
        return BOOSTS.getData().containsKey(boostResourceLocation);
    }

    public static Collection<ResourceLocation> boostKeys(){
        return BOOSTS.getData().keySet();
    }

    public static Boost getRandomBoost(RandomSource random) {
        if(BOOSTS.getData().isEmpty()){
            return null;
        }
        return GeneralUtils.getRandomItem(new ArrayList<>(BOOSTS.getData().values()), random);
    }

    public static Boost getRandomBoost(RandomSource random, List<Boost> whitelist, List<Boost> blacklist) {
        if(BOOSTS.getData().isEmpty()){
            return null;
        }
        List<Boost> filtered = BOOSTS.getData().values().stream().filter(boost -> (whitelist.isEmpty() && boost.getBoostGroup() != Boost.BoostGroup.SPECIAL) || whitelist.contains(boost)).filter(boost -> !blacklist.contains(boost)).collect(Collectors.toList());
        return GeneralUtils.getRandomItem(filtered, random);
    }

    public static Boost getRandomBoostForEntity(RandomSource random, LivingEntity livingEntity, List<Boost> whitelist, List<Boost> blacklist, Map<Boost, Boost.Rarity> rarityOverrides) {
        if(BOOSTS.getData().isEmpty()){
            return null;
        }
        AppliedBoostsData cap = ((IAppliedBoostsDataHolder) livingEntity).getOrCreateAppliedBoostsData();
        List<Pair<Boost, Integer>> filtered = BOOSTS.getData().values().stream()
                .filter(boost -> (whitelist.isEmpty() && !getRarity(boost, rarityOverrides).equals(Boost.Rarity.NONE)) || whitelist.contains(boost))
                .filter(boost -> !blacklist.contains(boost))
                .filter(boost -> cap.getBoostsOfType(boost.getBoostGroup()).size() < boost.getBoostGroup().getMax())
                .filter(boost -> boost.canApply(livingEntity))
                .map(boost -> new Pair<>(boost, getRarity(boost, rarityOverrides).getWeight()))
                .collect(Collectors.toList());
        if(filtered.isEmpty()){
            return null;
        }
        return GeneralUtils.getRandomEntry(filtered, random);
    }

    private static Boost.Rarity getRarity(Boost boost, Map<Boost, Boost.Rarity> rarityOverrides){
        if(rarityOverrides.containsKey(boost)){
            return rarityOverrides.get(boost);
        }else{
            return boost.getRarity();
        }
    }
}
