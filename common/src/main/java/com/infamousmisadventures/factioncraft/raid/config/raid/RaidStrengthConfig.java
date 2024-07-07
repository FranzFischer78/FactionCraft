package com.infamousmisadventures.factioncraft.raid.config.raid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.Difficulty;

public class RaidStrengthConfig {
    public static final RaidStrengthConfig DEFAULT = new RaidStrengthConfig(0.65F, 0.15F, 0.1F, -0.1F, 0F, 0.1F, 0.1F, 0.7F);
    public static final Codec<RaidStrengthConfig> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.FLOAT.optionalFieldOf("baseMultiplier", 0.65F).forGetter(RaidStrengthConfig::getBaseMultiplier),
                    Codec.FLOAT.optionalFieldOf("multiplierAdjustmentPerWave", 0.15F).forGetter(RaidStrengthConfig::getMultiplierAdjustmentPerWave),
                    Codec.FLOAT.optionalFieldOf("multiplierSpread", 0.1F).forGetter(RaidStrengthConfig::getMultiplierSpread),
                    Codec.FLOAT.optionalFieldOf("multiplierEasy", -0.1F).forGetter(RaidStrengthConfig::getMultiplierEasy),
                    Codec.FLOAT.optionalFieldOf("multiplierNormal", 0F).forGetter(RaidStrengthConfig::getMultiplierNormal),
                    Codec.FLOAT.optionalFieldOf("multiplierHard", 0.1F).forGetter(RaidStrengthConfig::getMultiplierHard),
                    Codec.FLOAT.optionalFieldOf("multiplierPerOmenLevel", 0.1F).forGetter(RaidStrengthConfig::getMultiplierPerOmenLevel),
                    Codec.FLOAT.optionalFieldOf("mobsFraction", 0.7F).forGetter(RaidStrengthConfig::getMobsFraction)
            ).apply(builder, RaidStrengthConfig::new));

    private final float baseMultiplier;
    private final float multiplierAdjustmentPerWave;
    private final float multiplierSpread;
    private final float multiplierEasy;
    private final float multiplierNormal;
    private final float multiplierHard;
    private final float multiplierPerOmenLevel;
    private final float mobsFraction;

    public RaidStrengthConfig(float baseMultiplier, float multiplierAdjustmentPerWave, float multiplierSpread, float multiplierEasy, float multiplierNormal, float multiplierHard, float multiplierPerOmenLevel, float mobsFraction) {
        this.baseMultiplier = baseMultiplier;
        this.multiplierAdjustmentPerWave = multiplierAdjustmentPerWave;
        this.multiplierSpread = multiplierSpread;
        this.multiplierEasy = multiplierEasy;
        this.multiplierNormal = multiplierNormal;
        this.multiplierHard = multiplierHard;
        this.multiplierPerOmenLevel = multiplierPerOmenLevel;
        this.mobsFraction = mobsFraction;
    }

    public float getBaseMultiplier() {
        return baseMultiplier;
    }

    public float getMultiplierAdjustmentPerWave() {
        return multiplierAdjustmentPerWave;
    }

    public float getMultiplierSpread() {
        return multiplierSpread;
    }

    public float getMultiplierEasy() {
        return multiplierEasy;
    }

    public float getMultiplierNormal() {
        return multiplierNormal;
    }

    public float getMultiplierHard() {
        return multiplierHard;
    }

    public float getMultiplierPerOmenLevel() {
        return multiplierPerOmenLevel;
    }

    public float getMobsFraction() {
        return mobsFraction;
    }

    public float getDifficultyMultiplier(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> getMultiplierEasy();
            case NORMAL -> getMultiplierNormal();
            case HARD -> getMultiplierHard();
            default -> 0;
        };
    }
}
