package com.infamousmisadventures.factioncraft.boost;

import com.infamousmisadventures.factioncraft.entity.data.holder.IAppliedBoostsDataHolder;
import com.infamousmisadventures.factioncraft.registry.FCBoosts;
import com.infamousmisadventures.factioncraft.registry.FCRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.Map;

public abstract class Boost {
    public static final Codec<Boost> CODEC = FCRegistries.BOOST_TYPE.byNameCodec().dispatch(Boost::type, BoostType::codec);

    public abstract Codec<? extends Boost> getCodec();
    public abstract BoostGroup getBoostGroup();
    public abstract Rarity getRarity();

    public int apply(LivingEntity livingEntity){
        ((IAppliedBoostsDataHolder) livingEntity).getOrCreateAppliedBoostsData().addAppliedBoost(this);
        return 0;
    }

    public abstract boolean canApply(LivingEntity livingEntity);

    public void applyAIChanges(Mob mobEntity){
        // noop
    }

    public CompoundTag save(CompoundTag compoundNBT) {
        ResourceLocation resourceLocation = FCBoosts.BOOSTS.getData().entrySet().stream().filter(entry -> entry.getValue().equals(this)).findFirst().map(Map.Entry::getKey).orElse(new ResourceLocation("empty"));
        compoundNBT.putString("name", resourceLocation.toString());
        return compoundNBT;
    }

    public static Boost load(CompoundTag compoundNBT) {
        ResourceLocation name = new ResourceLocation(compoundNBT.getString("name"));
        return FCBoosts.getBoost(name);
    }

    public abstract BoostType<? extends Boost> type();

    public enum BoostGroup {
        SPECIAL("special", 999),
        ATTRIBUTE("attribute",999),
        ARMOR("armor", 1),
        MOUNT("mount", 1),
        MAINHAND("mainhand", 1),
        OFFHAND("offhand", 1),
        AI("ai", 10),
        ROLE("role", 1),;
        public static final Codec<BoostGroup> CODEC = Codec.STRING.flatComapMap(s -> BoostGroup.byName(s, null), d -> DataResult.success(d.getName()));

        private final String name;
        private final int max;

        BoostGroup(String name, int max) {
            this.name = name;
            this.max = max;
        }

        public int getMax() {
            return max;
        }

        public String getName() {
            return name;
        }

        public static BoostGroup byName(String key, BoostGroup fallBack) {
            for(BoostGroup boostGroup : values()) {
                if (boostGroup.name.equalsIgnoreCase(key)) {
                    return boostGroup;
                }
            }

            return fallBack;
        }

        public static BoostGroup create(String id, String name, int max)
        {
            throw new IllegalStateException("Enum not extended");
        }
    }

    public enum Rarity {
        SUPER_COMMON("super_common", 40),
        COMMON("common", 10),
        UNCOMMON("uncommon", 5),
        RARE("rare", 2),
        VERY_RARE("very_rare", 1),
        NONE("none", 0);
        public static final Codec<Rarity> CODEC = Codec.STRING.flatComapMap(s -> Rarity.byName(s, null), d -> DataResult.success(d.getName()));

        private final String name;
        private final int weight;

        Rarity(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }

        /**
         * Retrieves the weight of Rarity.
         */
        public int getWeight() {
            return this.weight;
        }

        public String getName() {
            return name;
        }

        public static Rarity byName(String key, Rarity fallBack) {
            for(Rarity rarity : values()) {
                if (rarity.name.equalsIgnoreCase(key)) {
                    return rarity;
                }
            }

            return fallBack;
        }
    }
}
