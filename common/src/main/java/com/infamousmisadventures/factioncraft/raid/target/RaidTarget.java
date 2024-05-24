package com.infamousmisadventures.factioncraft.raid.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.infamousmisadventures.factioncraft.raid.Raid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.IExtensibleEnum;

public interface RaidTarget {

    BlockPos getTargetBlockPos();

    void updateTargetBlockPos(ServerLevel level);

    int getTargetStrength();

    void increaseTargetStrength(int amount);

    int getAdditionalWaves();

    boolean isDefeat(Raid raid, ServerLevel level);

    CompoundTag save(CompoundTag compoundNbt);

    boolean isValidSpawnPos(int outerAttempt, BlockPos.MutableBlockPos blockpos$mutable, ServerLevel level);

    Type getRaidType();

    int getStartingWave();

    float getSpawnDistance();

    enum Type implements IExtensibleEnum {
        VILLAGE("village"),
        PLAYER("player"),
        BATTLE("battle");
        public static final Codec<Type> CODEC = Codec.STRING.flatComapMap(s -> Type.byName(s, null), d -> DataResult.success(d.getName()));

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Type byName(String key, Type fallBack) {
            for(Type raidTargetType : values()) {
                if (raidTargetType.name.equalsIgnoreCase(key)) {
                    return raidTargetType;
                }
            }

            return fallBack;
        }

        public static Type create(String id, String name)
        {
            throw new IllegalStateException("Enum not extended");
        }
    }
}
