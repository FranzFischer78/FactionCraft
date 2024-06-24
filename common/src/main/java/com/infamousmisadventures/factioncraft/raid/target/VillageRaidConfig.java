package com.infamousmisadventures.factioncraft.raid.target;

import com.infamousmisadventures.factioncraft.event.CalculateStrengthEvent;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.raid.Raid;
import com.infamousmisadventures.factioncraft.registry.FCFactions;
import com.infamousmisadventures.factioncraft.registry.FCRaidConfigTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.infamousmisadventures.factioncraft.config.FactionCraftConfig.*;

public class VillageRaidConfig implements RaidConfig {

    private final RaidConfigType type;

    private final RaidWaveConfig raidWaveConfig;
    private final RaidStrengthConfig raidStrengthConfig;
    private final Component raidBarNameComponent;
    private final Component raidBarVictoryComponent;
    private final Component raidBarDefeatComponent;
    private final Optional<Holder<SoundEvent>> waveSoundEvent;
    private final Optional<Holder<SoundEvent>> victorySoundEvent;
    private final Optional<Holder<SoundEvent>> defeatSoundEvent;

    //AdditionalData
    private Faction faction;
    private BlockPos blockPos;
    private int targetStrength;

    public VillageRaidConfig(RaidConfigType type, RaidWaveConfig raidWaveConfig, RaidStrengthConfig raidStrengthConfig, String raidBarName, String raidBarVictory, String raidBarDefeat, Optional<Holder<SoundEvent>> waveSoundEvent, Optional<Holder<SoundEvent>> victorySoundEvent, Optional<Holder<SoundEvent>> defeatSoundEvent) {
        this.type = type;
        this.raidStrengthConfig = raidStrengthConfig;
        this.raidWaveConfig = raidWaveConfig;
        this.raidBarNameComponent = Component.translatable(raidBarName);
        this.raidBarVictoryComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarVictory));
        this.raidBarDefeatComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarDefeat));
        this.waveSoundEvent = waveSoundEvent;
        this.victorySoundEvent = victorySoundEvent;
        this.defeatSoundEvent = defeatSoundEvent;
    }

    public void init(Faction faction, BlockPos blockPos, ServerLevel level) {
        this.faction = faction;
        this.blockPos = blockPos;
        updateTargetBlockPos(level);
        this.targetStrength = calculateTargetStrength(blockPos, level);
    }

    public void init(Faction faction, BlockPos blockPos, int targetStrength) {
        this.faction = faction;
        this.blockPos = blockPos;
        this.targetStrength = targetStrength;
    }

    private int calculateTargetStrength(BlockPos blockPos, ServerLevel level) {
        int strength = 0;
        strength += level.getEntitiesOfClass(AbstractVillager.class,
                new AABB(blockPos).inflate(100),
                abstractVillagerEntity -> true).size() * VILLAGE_RAID_VILLAGER_WEIGHT.get();
        strength += level.getEntitiesOfClass(IronGolem.class,
                new AABB(blockPos).inflate(100),
                ironGolemEntity -> true).size() * VILLAGE_RAID_IRON_GOLEM_WEIGHT.get();
        CalculateStrengthEvent event = new CalculateStrengthEvent(this, blockPos, level, strength, strength);
        Services.EVENT_BUS.post(event);
        return (int) Math.floor(event.getStrength());
    }

    @Override
    public BlockPos getTargetBlockPos() {
        return blockPos;
    }

    @Override
    public void updateTargetBlockPos(ServerLevel level) {
        if (!level.isVillage(blockPos)) {
            this.moveRaidCenterToNearbyVillageSection(level);
        }
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    @Override
    public int getTargetStrength() {
        return targetStrength;
    }

    @Override
    public void increaseTargetStrength(int amount) {
        this.targetStrength += amount;
    }

    @Override
    public int getAdditionalWaves() {
        return (int) Math.floor(VILLAGE_RAID_ADDITIONAL_WAVE_CHANCE.get() * targetStrength);
    }

    @Override
    public boolean isDefeat(Raid raid, ServerLevel level) {
        if (level.getGameTime() % 20 == 0) {
            if (level.getEntitiesOfClass(AbstractVillager.class,
                    new AABB(blockPos).inflate(100),
                    abstractVillagerEntity -> !abstractVillagerEntity.isBaby()).size() == 0) {
                return true;
            }
        }
        return !level.isVillage(blockPos);
    }

    @Override
    public boolean isValidSpawnPos(int outerAttempt, BlockPos.MutableBlockPos blockpos$mutable, ServerLevel level) {
        return (!level.isVillage(blockpos$mutable) || outerAttempt >= 2)
                && level.hasChunksAt(blockpos$mutable.getX() - 10, blockpos$mutable.getY() - 10, blockpos$mutable.getZ() - 10, blockpos$mutable.getX() + 10, blockpos$mutable.getY() + 10, blockpos$mutable.getZ() + 10)
                && level.isPositionEntityTicking(blockpos$mutable)
                && (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, level, blockpos$mutable, EntityType.RAVAGER)
                || level.getBlockState(blockpos$mutable.below()).is(Blocks.SNOW) && level.getBlockState(blockpos$mutable).isAir());
    }

    @Override
    public RaidWaveConfig getRaidWaveConfig() {
        return raidWaveConfig;
    }

    @Override
    public float getSpawnDistance() {
        return 32.0F;
    }

    @Override
    public RaidConfigType type() {
        return type;
    }

    @Override
    public Component getRaidBarNameComponent() {
        return raidBarNameComponent;
    }

    @Override
    public Component getRaidBarVictoryComponent() {
        return raidBarVictoryComponent;
    }

    @Override
    public Component getRaidBarDefeatComponent() {
        return raidBarDefeatComponent;
    }

    @Override
    public RaidStrengthConfig getRaidStrengthConfig() {
        return raidStrengthConfig;
    }

    @Override
    public Optional<Holder<SoundEvent>> getWaveSoundEvent() {
        return waveSoundEvent;
    }

    @Override
    public Optional<Holder<SoundEvent>> getVictorySoundEvent() {
        return victorySoundEvent;
    }

    @Override
    public Optional<Holder<SoundEvent>> getDefeatSoundEvent() {
        return defeatSoundEvent;
    }

    private void moveRaidCenterToNearbyVillageSection(ServerLevel level) {
        Stream<SectionPos> stream = SectionPos.cube(SectionPos.of(blockPos), 2);
        stream.filter(level::isVillage).map(SectionPos::center).min(Comparator.comparingDouble((p_223025_1_) -> {
            return p_223025_1_.distSqr(blockPos);
        })).ifPresent(this::setBlockPos);
    }

    @Override
    public void loadAdditionalData(ServerLevel level, CompoundTag compoundNBT) {
        ResourceLocation factionName = new ResourceLocation(compoundNBT.getString("Faction"));
        if (FCFactions.factionExists(factionName)) {
            Faction faction = FCFactions.getFaction(factionName);
            init(
                    faction,
                    new BlockPos(compoundNBT.getInt("X"), compoundNBT.getInt("Y"), compoundNBT.getInt("Z")),
                    compoundNBT.getInt("TargetStrength")
            );
        }
    }

    @Override
    public Map<Faction, Integer> determineFactionFractions(int targetStrength) {
        Map<Faction, Integer> factionFractions = new HashMap<>();
        factionFractions.put(faction, targetStrength);
        return factionFractions;
    }

    @Override
    public CompoundTag saveAdditionalData(CompoundTag compoundNbt) {
        compoundNbt.putString("Type", FCRaidConfigTypes.getKey(this.type()).toString());
        compoundNbt.putString("Faction", faction.getName().toString());
        compoundNbt.putInt("X", blockPos.getX());
        compoundNbt.putInt("Y", blockPos.getY());
        compoundNbt.putInt("Z", blockPos.getZ());
        compoundNbt.putInt("TargetStrength", targetStrength);
        return compoundNbt;
    }
}
