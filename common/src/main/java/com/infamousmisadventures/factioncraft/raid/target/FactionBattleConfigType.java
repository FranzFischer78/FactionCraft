package com.infamousmisadventures.factioncraft.raid.target;

import com.infamousmisadventures.factioncraft.registry.FCRaidConfigBaseTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.Optional;

public class FactionBattleConfigType extends RaidConfigType {
    public static final FactionBattleConfigType DEFAULT = new FactionBattleConfigType("event.minecraft.raid", "event.minecraft.raid.victory", "event.minecraft.raid.defeat", DEFAULT_MOBS_FRACTION, Optional.of(SoundEvents.RAID_HORN), Optional.empty(), Optional.of(SoundEvents.RAID_HORN));
    public static final Codec<FactionBattleConfigType> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.STRING.optionalFieldOf("raid_bar_name", "event.minecraft.raid").forGetter((FactionBattleConfigType config) -> config.getRaidBarNameComponent().getString()),
                    Codec.STRING.optionalFieldOf("raid_bar_victory", "event.minecraft.raid.victory").forGetter((FactionBattleConfigType config) -> config.getRaidBarVictoryComponent().getString()),
                    Codec.STRING.optionalFieldOf("raid_bar_defeat", "event.minecraft.raid.defeat").forGetter((FactionBattleConfigType config) -> config.getRaidBarDefeatComponent().getString()),
                    Codec.FLOAT.optionalFieldOf("mobs_fraction", DEFAULT_MOBS_FRACTION).forGetter(FactionBattleConfigType::getMobsFraction),
                    SoundEvent.CODEC.optionalFieldOf("wave_sound").forGetter(FactionBattleConfigType::getWaveSoundEvent),
                    SoundEvent.CODEC.optionalFieldOf("victory_sound").forGetter(FactionBattleConfigType::getVictorySoundEvent),
                    SoundEvent.CODEC.optionalFieldOf("defeat_sound").forGetter(FactionBattleConfigType::getDefeatSoundEvent)
            ).apply(builder, FactionBattleConfigType::new));

    private final Component raidBarNameComponent;
    private final Component raidBarVictoryComponent;
    private final Component raidBarDefeatComponent;
    private final float mobsFraction;
    private final Optional<Holder<SoundEvent>> waveSoundEvent;
    private final Optional<Holder<SoundEvent>> victorySoundEvent;
    private final Optional<Holder<SoundEvent>> defeatSoundEvent;

    public FactionBattleConfigType(String raidBarName, String raidBarVictory, String raidBarDefeat, float mobsFraction, Optional<Holder<SoundEvent>> waveSoundEvent, Optional<Holder<SoundEvent>> victorySoundEvent, Optional<Holder<SoundEvent>> defeatSoundEvent) {
        this.raidBarNameComponent = Component.translatable(raidBarName);
        this.raidBarVictoryComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarVictory));
        this.raidBarDefeatComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarDefeat));
        this.mobsFraction = mobsFraction;
        this.waveSoundEvent = waveSoundEvent;
        this.victorySoundEvent = victorySoundEvent;
        this.defeatSoundEvent = defeatSoundEvent;
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
    public RaidConfigBaseType<? extends RaidConfigType> baseType() {
        return FCRaidConfigBaseTypes.VILLAGE.get();
    }

    @Override
    public RaidConfig create() {
        return new FactionBattleConfig(
                this,
                raidBarNameComponent.getString(),
                raidBarVictoryComponent.getString(),
                raidBarDefeatComponent.getString(),
                mobsFraction,
                waveSoundEvent,
                victorySoundEvent,
                defeatSoundEvent
        );
    }
}
