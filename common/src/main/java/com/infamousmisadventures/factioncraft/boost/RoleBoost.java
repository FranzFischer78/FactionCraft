package com.infamousmisadventures.factioncraft.boost;

import com.infamousmisadventures.factioncraft.registry.FCBoosts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class RoleBoost extends Boost {

    public static final Codec<RoleBoost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("boosts").forGetter(RoleBoost::getBoosts),
            ResourceLocation.CODEC.listOf().optionalFieldOf("fallback_boosts", new ArrayList<>()).forGetter(RoleBoost::getFallbackBoosts),
            Codec.INT.optionalFieldOf("strength_adjustment", 0).forGetter(RoleBoost::getStrengthAdjustment),
            BoostGroup.CODEC.optionalFieldOf("boost_type", BoostGroup.ROLE).forGetter(RoleBoost::getBoostGroup),
            Rarity.CODEC.fieldOf("rarity").forGetter(RoleBoost::getRarity)
    ).apply(instance, RoleBoost::new));

    private final List<ResourceLocation> boosts;
    private final List<ResourceLocation> fallbackBoosts;
    private final int strengthAdjustment;
    private final BoostGroup boostGroup;
    private final Rarity rarity;

    public RoleBoost(List<ResourceLocation> boosts, List<ResourceLocation> fallbackBoosts, int strengthAdjustment, BoostGroup boostGroup, Rarity rarity) {
        this.boosts = boosts;
        this.fallbackBoosts = fallbackBoosts;
        this.strengthAdjustment = strengthAdjustment;
        this.boostGroup = boostGroup;
        this.rarity = rarity;
    }

    public List<ResourceLocation> getBoosts() {
        return boosts;
    }

    public List<ResourceLocation> getFallbackBoosts() {
        return fallbackBoosts;
    }

    public int getStrengthAdjustment() {
        return strengthAdjustment;
    }

    public BoostGroup getBoostType() {
        return boostGroup;
    }

    @Override
    public Codec<? extends Boost> getCodec() {
        return CODEC;
    }

    @Override
    public BoostGroup getBoostGroup() {
        return boostGroup;
    }


    @Override
    public Rarity getRarity() {
        return rarity;
    }
    @Override
    public int apply(LivingEntity livingEntity) {
        if (!canApply(livingEntity)) {
            return 0;
        }
        List<Boost> fallbacks = boosts.stream().map(FCBoosts::getBoost).filter(Objects::nonNull).toList();
        Integer boostsStrength = boosts.stream().map(FCBoosts::getBoost).filter(Objects::nonNull).flatMap(boost -> boost.canApply(livingEntity) ? Stream.of(boost) : fallbacks.stream()).map(boost -> boost.apply(livingEntity)).reduce(0, Integer::sum);
        super.apply(livingEntity);
        return boostsStrength + strengthAdjustment;
    }

    @Override
    public boolean canApply(LivingEntity livingEntity) {
        return livingEntity instanceof Mob;
    }
}
