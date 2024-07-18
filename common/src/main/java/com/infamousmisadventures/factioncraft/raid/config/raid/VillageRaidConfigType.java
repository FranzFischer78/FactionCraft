package com.infamousmisadventures.factioncraft.raid.config.raid;

import com.infamousmisadventures.factioncraft.registry.FCRaidConfigBaseTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.Optional;

public class VillageRaidConfigType extends RaidConfigType {
    public static final VillageRaidConfigType DEFAULT = new VillageRaidConfigType(RaidWaveConfig.DEFAULT, RaidStrengthConfig.DEFAULT, RaidSpawnPosConfig.DEFAULT, "event.minecraft.raid", "event.minecraft.raid.victory", "event.minecraft.raid.defeat", Optional.of(SoundEvents.RAID_HORN), Optional.empty(), Optional.of(SoundEvents.RAID_HORN));
    public static final Codec<VillageRaidConfigType> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    RaidWaveConfig.CODEC.optionalFieldOf("wave_raid_config", RaidWaveConfig.DEFAULT).forGetter(VillageRaidConfigType::getRaidWaveConfig),
                    RaidStrengthConfig.CODEC.optionalFieldOf("raid_strength_config", RaidStrengthConfig.DEFAULT).forGetter(VillageRaidConfigType::getRaidStrengthConfig),
                    RaidSpawnPosConfig.CODEC.optionalFieldOf("raid_spawn_pos_config", RaidSpawnPosConfig.DEFAULT).forGetter(VillageRaidConfigType::getRaidSpawnPosConfig),
                    Codec.STRING.optionalFieldOf("raid_bar_name", "event.minecraft.raid").forGetter((VillageRaidConfigType config) -> config.getRaidBarNameComponent().getString()),
                    Codec.STRING.optionalFieldOf("raid_bar_victory", "event.minecraft.raid.victory").forGetter((VillageRaidConfigType config) -> config.getRaidBarVictoryComponent().getString()),
                    Codec.STRING.optionalFieldOf("raid_bar_defeat", "event.minecraft.raid.defeat").forGetter((VillageRaidConfigType config) -> config.getRaidBarDefeatComponent().getString()),
                    SoundEvent.CODEC.optionalFieldOf("wave_sound").forGetter(VillageRaidConfigType::getWaveSoundEvent),
                    SoundEvent.CODEC.optionalFieldOf("victory_sound").forGetter(VillageRaidConfigType::getVictorySoundEvent),
                    SoundEvent.CODEC.optionalFieldOf("defeat_sound").forGetter(VillageRaidConfigType::getDefeatSoundEvent)
            ).apply(builder, VillageRaidConfigType::new));

    private final RaidWaveConfig raidWaveConfig;
    private final RaidStrengthConfig raidStrengthConfig;
    private final RaidSpawnPosConfig raidSpawnPosConfig;
    private final Component raidBarNameComponent;
    private final Component raidBarVictoryComponent;
    private final Component raidBarDefeatComponent;
    private final Optional<Holder<SoundEvent>> waveSoundEvent;
    private final Optional<Holder<SoundEvent>> victorySoundEvent;
    private final Optional<Holder<SoundEvent>> defeatSoundEvent;

    public VillageRaidConfigType(RaidWaveConfig raidWaveConfig, RaidStrengthConfig raidStrengthConfig, RaidSpawnPosConfig raidSpawnPosConfig, String raidBarName, String raidBarVictory, String raidBarDefeat, Optional<Holder<SoundEvent>> waveSoundEvent, Optional<Holder<SoundEvent>> victorySoundEvent, Optional<Holder<SoundEvent>> defeatSoundEvent) {
        this.raidWaveConfig = raidWaveConfig;
        this.raidStrengthConfig = raidStrengthConfig;
        this.raidSpawnPosConfig = raidSpawnPosConfig;
        this.raidBarNameComponent = Component.translatable(raidBarName);
        this.raidBarVictoryComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarVictory));
        this.raidBarDefeatComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarDefeat));
        this.waveSoundEvent = waveSoundEvent;
        this.victorySoundEvent = victorySoundEvent;
        this.defeatSoundEvent = defeatSoundEvent;
    }

    public RaidWaveConfig getRaidWaveConfig() {
        return raidWaveConfig;
    }

    public RaidSpawnPosConfig getRaidSpawnPosConfig() {
        return raidSpawnPosConfig;
    }

    public Component getRaidBarNameComponent() {
        return raidBarNameComponent;
    }

    public Component getRaidBarVictoryComponent() {
        return raidBarVictoryComponent;
    }

    public Component getRaidBarDefeatComponent() {
        return raidBarDefeatComponent;
    }

    public RaidStrengthConfig getRaidStrengthConfig() {
        return raidStrengthConfig;
    }

    public Optional<Holder<SoundEvent>> getWaveSoundEvent() {
        return waveSoundEvent;
    }

    public Optional<Holder<SoundEvent>> getVictorySoundEvent() {
        return victorySoundEvent;
    }

    public Optional<Holder<SoundEvent>> getDefeatSoundEvent() {
        return defeatSoundEvent;
    }

    @Override
    public RaidConfigBaseType<? extends RaidConfigType> baseType() {
        return FCRaidConfigBaseTypes.VILLAGE.get();
    }

    @Override
    public RaidConfig create() {
        return new VillageRaidConfig(
                this,
                raidWaveConfig,
                raidStrengthConfig,
                raidSpawnPosConfig,
                raidBarNameComponent.getString(),
                raidBarVictoryComponent.getString(),
                raidBarDefeatComponent.getString(),
                waveSoundEvent,
                victorySoundEvent,
                defeatSoundEvent
        );
    }
}
