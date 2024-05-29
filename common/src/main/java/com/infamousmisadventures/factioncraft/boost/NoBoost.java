package com.infamousmisadventures.factioncraft.boost;

import com.infamousmisadventures.factioncraft.registry.FCBoostTypes;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.LivingEntity;

import static com.infamousmisadventures.factioncraft.boost.Boost.BoostGroup.SPECIAL;

public class NoBoost extends Boost {

    public static final NoBoost INSTANCE = new NoBoost();
    public static final Codec<NoBoost> CODEC = Codec.unit(INSTANCE);

    public NoBoost()
    {
        super();
    }

    @Override
    public Codec<? extends Boost> getCodec() {
        return CODEC;
    }

    @Override
    public BoostGroup getBoostGroup() {
        return SPECIAL;
    }

    @Override
    public BoostType<? extends Boost> type() {
        return FCBoostTypes.NO_BOOST.get();
    }

    @Override
    public Rarity getRarity() {
        return Rarity.NONE;
    }

    @Override
    public int apply(LivingEntity livingEntity) {
        return 0;
    }

    @Override
    public boolean canApply(LivingEntity livingEntity) {
        return true;
    }
}
