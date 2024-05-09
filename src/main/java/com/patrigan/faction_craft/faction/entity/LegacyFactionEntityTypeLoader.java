package com.patrigan.faction_craft.faction.entity;

import com.patrigan.faction_craft.data.ResourceSet;
import com.patrigan.faction_craft.registry.FactionEntityTypes;
import com.patrigan.faction_craft.util.IntRange;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to load FactionEntityTypes from a CompoundTag.
 *
 * @deprecated This class is deprecated as of version 1.19.2-1.4.0 and is scheduled for removal in 1.21.0-1.5.0.
 * The functionality provided by this class has been superseded by the use of FactionEntityTypes in their own registry.
 * Please use the new registry for FactionEntityTypes instead of this class.
 */
@Deprecated(forRemoval = true)
public class LegacyFactionEntityTypeLoader {
    public static FactionEntityType load(CompoundTag compoundNbt) {
        if (compoundNbt.contains("factionEntityType")) {
            ResourceLocation factionEntityType = new ResourceLocation(compoundNbt.getString("factionEntityType"));
            if (FactionEntityTypes.getFactionEntityType(factionEntityType) != null) {
                return FactionEntityTypes.getFactionEntityType(factionEntityType);
            } else {
                return FactionEntityType.DEFAULT;
            }
        } else {
            FactionEntityRank rank = FactionEntityRank.byName(compoundNbt.getString("rank"), FactionEntityRank.SOLDIER);
            FactionEntityRank maximumRank = FactionEntityRank.byName(compoundNbt.getString("maximumRank"), null);
            List<FactionEntityRank> ranks = new ArrayList<>();
            FactionEntityRank currentRank = rank;
            while (currentRank != null) {
                ranks.add(currentRank);
                if (currentRank.equals(maximumRank)) {
                    break;
                }
                currentRank = currentRank.promote();
            }
            return new FactionEntityType(
                    new ResourceLocation(compoundNbt.getString("entityType")),
                    compoundNbt.getCompound("tag"),
                    false,
                    true,
                    compoundNbt.getInt("weight"),
                    compoundNbt.getInt("strength"),
                    ranks,
                    EntityBoostConfig.load(compoundNbt.getCompound("entityBoostConfig")),
                    new IntRange(compoundNbt.getInt("minimumWave"),
                            compoundNbt.getInt("maximumWave")),
                    new IntRange(compoundNbt.getInt("minimumSpawned"),
                            compoundNbt.getInt("maximumSpawned")),
                    Integer.MAX_VALUE,
                    new IntRange(compoundNbt.getInt("minimumOmen"),
                            compoundNbt.getInt("maximumOmen")),
                    new IntRange(-64, 320),
                    ResourceSet.getEmpty(Registry.BIOME_REGISTRY),
                    ResourceSet.getEmpty(Registry.BIOME_REGISTRY),
                    Integer.MAX_VALUE
            );
        }
    }

    public static CompoundTag saveStatic(FactionEntityType factionEntityType, CompoundTag compoundNbt) {
        ResourceLocation factionEntityTypeResource = FactionEntityTypes.getFactionEntityTypeKey(factionEntityType);
        if (factionEntityType != null) {
            compoundNbt.putString("factionEntityType", factionEntityTypeResource.toString());
        } else {
            compoundNbt.putString("entityType", factionEntityType.getEntityTypeName().toString());
            compoundNbt.put("tag", factionEntityType.getTag());
            compoundNbt.putInt("weight", factionEntityType.getWeight());
            compoundNbt.putInt("strength", factionEntityType.getStrength());
            compoundNbt.putString("rank", factionEntityType.getRanks().get(0).getName());
            compoundNbt.putString("maximumRank", factionEntityType.getRanks().get(factionEntityType.getRanks().size() - 1).getName());
            CompoundTag boostConfigNbt = new CompoundTag();
            compoundNbt.put("entityBoostConfig", factionEntityType.getBoostConfig().save(boostConfigNbt));
            compoundNbt.putInt("minimumWave", factionEntityType.getWaveRange().min());
            compoundNbt.putInt("maximumWave", factionEntityType.getWaveRange().max());
            compoundNbt.putInt("minimumSpawned", factionEntityType.getSpawnedRange().min());
            compoundNbt.putInt("maximumSpawned", factionEntityType.getSpawnedRange().max());
            compoundNbt.putInt("minimumOmen", factionEntityType.getOmenRange().min());
            compoundNbt.putInt("maximumOmen", factionEntityType.getOmenRange().max());
        }
        return compoundNbt;
    }
}
