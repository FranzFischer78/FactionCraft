package com.infamousmisadventures.factioncraft.raid.config.raid;

import com.infamousmisadventures.factioncraft.raid.config.wave.WaveConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

public class RaidSpawnPosConfig {
    public static final RaidSpawnPosConfig DEFAULT = new RaidSpawnPosConfig(YLevelType.WORLD_SURFACE);
    public static final Codec<RaidSpawnPosConfig> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    YLevelType.CODEC.optionalFieldOf("y_level_type", YLevelType.WORLD_SURFACE).forGetter(RaidSpawnPosConfig::getYLevelType)
            ).apply(builder, RaidSpawnPosConfig::new));

    private final YLevelType yLevelType;

    public RaidSpawnPosConfig(YLevelType yLevelType) {
        this.yLevelType = yLevelType;
    }

    public YLevelType getYLevelType() {
        return yLevelType;
    }

    public BlockPos getRandomSpawnBlockPos(RaidConfig raidConfig, WaveConfig waveConfig, ServerLevel level, float i) {
        float f = level.random.nextFloat() * ((float) Math.PI * 2F);
        int j = raidConfig.getTargetBlockPos().getX() + Mth.floor(Mth.cos(f) * waveConfig.getSpawnDistance() * i) + level.random.nextInt(5);
        int l = raidConfig.getTargetBlockPos().getZ() + Mth.floor(Mth.sin(f) * waveConfig.getSpawnDistance() * i) + level.random.nextInt(5);
        int k = waveConfig.getRaidSpawnPosConfig().getSpawnPosY(level, j, l);
        return new BlockPos(j, k, l);
    }
    
    public boolean isValidSpawnPos(BlockPos.MutableBlockPos blockpos$mutable, ServerLevel level) {
        return level.hasChunksAt(blockpos$mutable.getX() - 10, blockpos$mutable.getY() - 10, blockpos$mutable.getZ() - 10, blockpos$mutable.getX() + 10, blockpos$mutable.getY() + 10, blockpos$mutable.getZ() + 10)
                && level.isPositionEntityTicking(blockpos$mutable)
                && (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, level, blockpos$mutable, EntityType.RAVAGER)
                || level.getBlockState(blockpos$mutable.below()).is(Blocks.SNOW) && level.getBlockState(blockpos$mutable).isAir());
    }

    public int getSpawnPosY(ServerLevel level, int x, int z) {
        return switch (yLevelType) {
            case WORLD_SURFACE -> level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            case OCEAN_FLOOR -> level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        };
    }

    public enum YLevelType {
        WORLD_SURFACE("world_surface"),
        OCEAN_FLOOR("ocean_floor");
        public static final Codec<YLevelType> CODEC = Codec.STRING.flatComapMap(s -> YLevelType.byName(s, null), d -> DataResult.success(d.getName()));

        private final String name;

        YLevelType(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }

        public static YLevelType byName(String key, YLevelType fallBack) {
            for(YLevelType yLevelType : values()) {
                if (yLevelType.name.equalsIgnoreCase(key)) {
                    return yLevelType;
                }
            }

            return fallBack;
        }
    }
}
