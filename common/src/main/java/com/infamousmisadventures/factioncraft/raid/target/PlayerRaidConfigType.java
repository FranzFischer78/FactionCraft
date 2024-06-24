package com.infamousmisadventures.factioncraft.raid.target;

import com.infamousmisadventures.factioncraft.registry.FCRaidConfigBaseTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.Optional;

public class PlayerRaidConfigType extends RaidConfigType {
    public static final PlayerRaidConfigType DEFAULT = new PlayerRaidConfigType(WaveRaidConfig.DEFAULT, DEFAULT_MOBS_FRACTION, "event.minecraft.raid", "event.minecraft.raid.victory", "event.minecraft.raid.defeat", Optional.of(SoundEvents.RAID_HORN), Optional.empty(), Optional.of(SoundEvents.RAID_HORN));
    public static final Codec<PlayerRaidConfigType> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    WaveRaidConfig.CODEC.optionalFieldOf("wave_raid_config", WaveRaidConfig.DEFAULT).forGetter(PlayerRaidConfigType::getWaveRaidConfig),
                    Codec.FLOAT.optionalFieldOf("mobs_fraction", DEFAULT_MOBS_FRACTION).forGetter(PlayerRaidConfigType::getMobsFraction),
                    Codec.STRING.optionalFieldOf("raid_bar_name", "event.minecraft.raid").forGetter((PlayerRaidConfigType config) -> config.getRaidBarNameComponent().getString()),
                    Codec.STRING.optionalFieldOf("raid_bar_victory", "event.minecraft.raid.victory").forGetter((PlayerRaidConfigType config) -> config.getRaidBarVictoryComponent().getString()),
                    Codec.STRING.optionalFieldOf("raid_bar_defeat", "event.minecraft.raid.defeat").forGetter((PlayerRaidConfigType config) -> config.getRaidBarDefeatComponent().getString()),
                    SoundEvent.CODEC.optionalFieldOf("wave_sound").forGetter(PlayerRaidConfigType::getWaveSoundEvent),
                    SoundEvent.CODEC.optionalFieldOf("victory_sound").forGetter(PlayerRaidConfigType::getVictorySoundEvent),
                    SoundEvent.CODEC.optionalFieldOf("defeat_sound").forGetter(PlayerRaidConfigType::getDefeatSoundEvent)
            ).apply(builder, PlayerRaidConfigType::new));

    private final WaveRaidConfig waveRaidConfig;
    private final Component raidBarNameComponent;
    private final Component raidBarVictoryComponent;
    private final Component raidBarDefeatComponent;
    private final float mobsFraction;
    private final Optional<Holder<SoundEvent>> waveSoundEvent;
    private final Optional<Holder<SoundEvent>> victorySoundEvent;
    private final Optional<Holder<SoundEvent>> defeatSoundEvent;

    public PlayerRaidConfigType(WaveRaidConfig waveRaidConfig, float mobsFraction, String raidBarName, String raidBarVictory, String raidBarDefeat, Optional<Holder<SoundEvent>> waveSoundEvent, Optional<Holder<SoundEvent>> victorySoundEvent, Optional<Holder<SoundEvent>> defeatSoundEvent) {
        this.waveRaidConfig = waveRaidConfig;
        this.mobsFraction = mobsFraction;
        this.raidBarNameComponent = Component.translatable(raidBarName);
        this.raidBarVictoryComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarVictory));
        this.raidBarDefeatComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarDefeat));
        this.waveSoundEvent = waveSoundEvent;
        this.victorySoundEvent = victorySoundEvent;
        this.defeatSoundEvent = defeatSoundEvent;
    }

    public WaveRaidConfig getWaveRaidConfig() {
        return waveRaidConfig;
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

    public float getMobsFraction() {
        return mobsFraction;
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
    public RaidConfig create() {
        return new PlayerRaidConfig(
                this,
                waveRaidConfig,
                raidBarNameComponent,
                raidBarVictoryComponent,
                raidBarDefeatComponent,
                mobsFraction,
                waveSoundEvent,
                victorySoundEvent,
                defeatSoundEvent
        );
    }

    @Override
    public RaidConfigBaseType<? extends RaidConfigType> baseType() {
        return FCRaidConfigBaseTypes.PLAYER.get();
    }
}
