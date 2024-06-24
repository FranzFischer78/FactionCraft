package com.infamousmisadventures.factioncraft.raid.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class WaveRaidConfig {
public static final WaveRaidConfig DEFAULT = new WaveRaidConfig(0, 2, 4, 6, 10);
    public static final Codec<WaveRaidConfig> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.INT.optionalFieldOf("starting_wave", 0).forGetter(WaveRaidConfig::getStartingWave),
                    Codec.INT.optionalFieldOf("number_waves_easy", 2).forGetter(WaveRaidConfig::getNumberWavesEasy),
                    Codec.INT.optionalFieldOf("number_waves_normal", 4).forGetter(WaveRaidConfig::getNumberWavesNormal),
                    Codec.INT.optionalFieldOf("number_waves_hard", 6).forGetter(WaveRaidConfig::getNumberWavesHard),
                    Codec.INT.optionalFieldOf("max_number_waves", 10).forGetter(WaveRaidConfig::getMaxNumberWaves)
            ).apply(builder, WaveRaidConfig::new));

    private final int startingWave;
    private final int numberWavesEasy;
    private final int numberWavesNormal;
    private final int numberWavesHard;
    private final int maxNumberWaves;

    public WaveRaidConfig(int startingWave, int numberWavesEasy, int numberWavesNormal, int numberWavesHard, int maxNumberWaves) {
        this.startingWave = startingWave;
        this.numberWavesEasy = numberWavesEasy;
        this.numberWavesNormal = numberWavesNormal;
        this.numberWavesHard = numberWavesHard;
        this.maxNumberWaves = maxNumberWaves;
    }

    public int getStartingWave() {
        return startingWave;
    }

    public int getNumberWavesEasy() {
        return numberWavesEasy;
    }

    public int getNumberWavesNormal() {
        return numberWavesNormal;
    }

    public int getNumberWavesHard() {
        return numberWavesHard;
    }

    public int getMaxNumberWaves() {
        return maxNumberWaves;
    }
}
