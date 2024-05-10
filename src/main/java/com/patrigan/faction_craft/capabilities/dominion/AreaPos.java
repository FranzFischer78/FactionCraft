package com.patrigan.faction_craft.capabilities.dominion;

import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;
import java.util.stream.IntStream;

public class AreaPos {

    public static final Codec<AreaPos> AREAPOS_CODEC = Codec.INT_STREAM.comapFlatMap(
                    (p_121967_) -> Util.fixedSize(p_121967_, 2)
                            .map((p_175270_) -> new AreaPos(p_175270_[0], p_175270_[1])),
                    (p_121924_) -> IntStream.of(p_121924_.x, p_121924_.z))
            .stable();

    public final int x;
    public final int z;

    public AreaPos(int pX, int pY) {
        this.x = pX;
        this.z = pY;
    }

    public AreaPos(BlockPos pPos) {
        this.x = blockToAreaCoord(pPos.getX()) ;
        this.z = blockToAreaCoord(pPos.getZ());
    }

    public AreaPos(ChunkPos pPos) {
        this.x = chunkToAreaCoord(pPos.x) ;
        this.z = chunkToAreaCoord(pPos.z);
    }

    public static int blockToAreaCoord(int pBlockCoord) {
        return (pBlockCoord >> 4) >> 2;
    }

    public static int chunkToAreaCoord(int pChunkCoord) {
        return pChunkCoord >> 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaPos areaPos = (AreaPos) o;
        return x == areaPos.x && z == areaPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
