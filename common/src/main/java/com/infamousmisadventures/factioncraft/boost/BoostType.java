package com.infamousmisadventures.factioncraft.boost;

import com.mojang.serialization.Codec;

public class BoostType<P extends Boost> {
    private final Codec<P> codec;

    public BoostType(Codec<P> codec) {
        this.codec = codec;
    }

    public Codec<P> codec() {
        return this.codec;
    }
}
