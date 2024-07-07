package com.infamousmisadventures.factioncraft.raid.config.raid;

import com.mojang.serialization.Codec;

public class RaidConfigBaseType<P extends RaidConfigType> {
    private final Codec<P> codec;

    public RaidConfigBaseType(Codec<P> codec) {
        this.codec = codec;
    }

    public Codec<P> codec() {
        return this.codec;
    }
}