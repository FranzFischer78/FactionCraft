package com.infamousmisadventures.factioncraft.faction.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.infamousmisadventures.factioncraft.capabilities.factionentity.FactionEntity;
import com.infamousmisadventures.factioncraft.capabilities.factionentity.FactionEntityHelper;
import com.infamousmisadventures.factioncraft.util.data.ResourceSet;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.util.IntRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES;


public class FactionEntityType {
    public static final FactionEntityType DEFAULT = new FactionEntityType(new ResourceLocation("minecraft:pig"), new CompoundTag(), false, true, 1, 1, List.of(FactionEntityRank.SOLDIER), EntityBoostConfig.DEFAULT, new IntRange(0, 10000), new IntRange(0, 10000), Integer.MAX_VALUE, new IntRange(0, 10000), new IntRange(-64, 320), ResourceSet.getEmpty(Registry.BIOME_REGISTRY), ResourceSet.getEmpty(Registry.BIOME_REGISTRY), Integer.MAX_VALUE);
    public static final Codec<FactionEntityType> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("entity_type").forGetter(data -> data.entityType),
                    CompoundTag.CODEC.optionalFieldOf("tag", new CompoundTag()).forGetter(data -> data.tag),
                    Codec.BOOL.optionalFieldOf("tag_first", false).forGetter(data -> data.tagFirst),
                    Codec.BOOL.optionalFieldOf("should_finalize_spawn", true).forGetter(data -> data.shouldFinalizeSpawn),
                    Codec.INT.fieldOf("weight").forGetter(data -> data.weight),
                    Codec.INT.fieldOf("strength").forGetter(data -> data.strength),
                    FactionEntityRank.CODEC.listOf().fieldOf("ranks").forGetter(data -> data.ranks),
                    EntityBoostConfig.CODEC.optionalFieldOf("boosts", EntityBoostConfig.DEFAULT).forGetter(data -> data.entityBoostConfig),
                    IntRange.getCodec(0, 10000).optionalFieldOf("wave_range", new IntRange(1, 10000)).forGetter(data -> data.waveRange),
                    IntRange.getCodec(0, 10000).optionalFieldOf("spawned_range", new IntRange(0, 10000)).forGetter(data -> data.spawnedRange),
                    Codec.INT.optionalFieldOf("max_spawned_per_x", Integer.MAX_VALUE).forGetter(data -> data.maxSpawnedPerX),
                    IntRange.getCodec(0, 10000).optionalFieldOf("omen_range", new IntRange(0, 10000)).forGetter(data -> data.omenRange),
                    IntRange.getCodec(-10000, 10000).optionalFieldOf("y_range", new IntRange(-64, 320)).forGetter(data -> data.yRange),
                    ResourceSet.getCodec(Registry.BIOME_REGISTRY).optionalFieldOf("biome_whitelist", ResourceSet.getEmpty(Registry.BIOME_REGISTRY)).forGetter(data -> data.biomeWhitelist),
                    ResourceSet.getCodec(Registry.BIOME_REGISTRY).optionalFieldOf("biome_blacklist", ResourceSet.getEmpty(Registry.BIOME_REGISTRY)).forGetter(data -> data.biomeBlacklist),
                    Codec.INT.optionalFieldOf("dominion_to_spawn", Integer.MAX_VALUE).forGetter(data -> data.dominionToSpawn)
            ).apply(builder, FactionEntityType::new));

    public static final Codec<FactionEntityType> CODEC_OLD = RecordCodecBuilder.create(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("entity_type").forGetter(data -> data.entityType),
                    CompoundTag.CODEC.optionalFieldOf("tag", new CompoundTag()).forGetter(data -> data.tag),
                    Codec.INT.fieldOf("weight").forGetter(data -> data.weight),
                    Codec.INT.fieldOf("strength").forGetter(data -> data.strength),
                    FactionEntityRank.CODEC.fieldOf("rank").forGetter(data -> data.ranks.get(0)),
                    FactionEntityRank.CODEC.fieldOf("maximum_rank").forGetter(data -> data.ranks.get(data.ranks.size()-1)),
                    EntityBoostConfig.CODEC.optionalFieldOf("boosts", EntityBoostConfig.DEFAULT).forGetter(data -> data.entityBoostConfig),
                    Codec.INT.fieldOf("minimum_wave").forGetter(data -> data.waveRange.getMin()),
                    Codec.INT.optionalFieldOf("maximum_wave", 10000).forGetter(data -> data.waveRange.getMax()),
                    Codec.INT.optionalFieldOf("minimum_spawned", 0).forGetter(data -> data.spawnedRange.getMin()),
                    Codec.INT.optionalFieldOf("maximum_spawned", 10000).forGetter(data -> data.spawnedRange.getMax()),
                    Codec.INT.optionalFieldOf("minimum_omen", 0).forGetter(data -> data.omenRange.getMin()),
                    Codec.INT.optionalFieldOf("maximum_omen", 10000).forGetter(data -> data.omenRange.getMax())
            ).apply(builder, FactionEntityType::new));

    private final ResourceLocation entityType;
    private final CompoundTag tag;
    private final boolean tagFirst;
    private final boolean shouldFinalizeSpawn;
    private final int weight;
    private final int strength;
    private final List<FactionEntityRank> ranks;
    private final EntityBoostConfig entityBoostConfig;
    private final IntRange waveRange;
    private final IntRange spawnedRange;
    private final int maxSpawnedPerX;
    private final IntRange omenRange;
    private final IntRange yRange;
    private final ResourceSet<Biome> biomeWhitelist;
    private final ResourceSet<Biome> biomeBlacklist;
    private final int dominionToSpawn;
    private MobSpawnSettings.SpawnerData spawnerData;

    public FactionEntityType(ResourceLocation entityType, CompoundTag tag, boolean tagFirst, boolean shouldFinalizeSpawn, int weight, int strength, List<FactionEntityRank> ranks, EntityBoostConfig entityBoostConfig, IntRange waveRange, IntRange spawnedRange, int maxSpawnedPerX, IntRange omenRange, IntRange yRange, ResourceSet<Biome> biomeWhitelist, ResourceSet<Biome> biomeBlacklist, int dominionToSpawn) {
        this.entityType = entityType;
        this.tag = tag;
        this.tagFirst = tagFirst;
        this.shouldFinalizeSpawn = shouldFinalizeSpawn;
        this.weight = weight;
        this.strength = strength;
        this.ranks = ranks;
        this.entityBoostConfig = entityBoostConfig;
        this.waveRange = waveRange;
        this.spawnedRange = spawnedRange;
        this.maxSpawnedPerX = maxSpawnedPerX <= 0 ? Integer.MAX_VALUE : maxSpawnedPerX;
        this.omenRange = omenRange;
        this.yRange = yRange;
        this.biomeWhitelist = biomeWhitelist;
        this.biomeBlacklist = biomeBlacklist;
        this.dominionToSpawn = dominionToSpawn;
    }

    public FactionEntityType(ResourceLocation entityType, CompoundTag tag, int weight, int strength, FactionEntityRank rank, FactionEntityRank maximumRank, EntityBoostConfig entityBoostConfig, int minimumWave, int maximumWave, int minimumSpawned, int maximumSpawned, int minimumOmen, int maximumOmen) {
        this.entityType = entityType;
        this.tag = tag;
        this.tagFirst = false;
        this.shouldFinalizeSpawn = true;
        this.weight = weight;
        this.strength = strength;
        this.ranks = new ArrayList<>();
        FactionEntityRank currentRank = rank;
        while (currentRank != null) {
            ranks.add(currentRank);
            if (currentRank.equals(maximumRank)) {
                break;
            }
            currentRank = currentRank.promote();
        }
        this.entityBoostConfig = entityBoostConfig;
        this.waveRange = new IntRange(minimumWave, maximumWave);
        this.spawnedRange = new IntRange(minimumSpawned, maximumSpawned);
        this.maxSpawnedPerX = Integer.MAX_VALUE;
        this.omenRange = new IntRange(minimumOmen, maximumOmen);
        this.yRange = new IntRange(-64, 320);
        this.biomeWhitelist = ResourceSet.getEmpty(Registry.BIOME_REGISTRY);
        this.biomeBlacklist = ResourceSet.getEmpty(Registry.BIOME_REGISTRY);
        this.dominionToSpawn = Integer.MAX_VALUE;
    }

    public ResourceLocation getEntityTypeName() {
        return entityType;
    }

    public EntityType<?> getEntityType() {
        return ENTITY_TYPES.getValue(entityType);
    }

    public CompoundTag getTag() {
        return tag;
    }

    public int getWeight() {
        return weight;
    }

    public int getSpawnWeight() {
        return weight;
    }

    public int getStrength() {
        return strength;
    }

    public EntityBoostConfig getBoostConfig() {
        return entityBoostConfig;
    }

    public IntRange getWaveRange() {
        return waveRange;
    }

    public IntRange getSpawnedRange() {
        return spawnedRange;
    }

    public int getMaxSpawnedPerX() {
        return maxSpawnedPerX;
    }

    public IntRange getOmenRange() {
        return omenRange;
    }

    public IntRange getYRange() {
        return yRange;
    }

    public ResourceSet<Biome> getBiomeWhitelist() {
        return biomeWhitelist;
    }

    public ResourceSet<Biome> getBiomeBlacklist() {
        return biomeBlacklist;
    }

    public boolean canSpawnInWave(int wave) {
        return getWaveRange().isBetweenInclusive(wave);
    }

    public boolean canSpawnForOmen(int omen) {
        return getOmenRange().isBetweenInclusive(omen);
    }

    public boolean canSpawnForYPos(BlockPos blockPos) {
        return blockPos == null || getYRange().isBetweenInclusive(blockPos.getY());
    }

    public boolean canSpawnForBiome(Biome biome) {
        if (biome == null) {
            return this.getBiomeWhitelist().isEmpty();
        } else {
            if (getBiomeBlacklist().contains(biome)) {
                return false;
            } else {
                return this.getBiomeWhitelist().isEmpty() || this.getBiomeWhitelist().contains(biome);
            }
        }
    }

    public boolean canSpawnForDominion(int dominion) {
        return dominion >= dominionToSpawn;
    }

    public List<FactionEntityRank> getRanks() {
        return ranks;
    }

    public boolean canBeBannerHolder() {
        List<FactionEntityRank> possibleCaptains = Arrays.asList(FactionEntityRank.CAPTAIN, FactionEntityRank.GENERAL, FactionEntityRank.LEADER);
        for (FactionEntityRank possibleCaptain : possibleCaptains) {
            if(ranks.contains(possibleCaptain)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRank(FactionEntityRank requiredRank) {
        return getRanks().contains(requiredRank);
    }

    public boolean hasRanks(List<FactionEntityRank> requiredRanks) {
        List<FactionEntityRank> possibleRanks = getRanks();
        return requiredRanks.stream().anyMatch(possibleRanks::contains);
    }

    public int getMaxSpawnedInGroup(int totalSpawned) {
        return getSpawnedRange().getMax() * Mth.ceil((totalSpawned+1) / (float) getMaxSpawnedPerX());
    }

    public void convertEntity(Faction faction, Entity entity){
        if(entity.level instanceof ServerLevel serverLevel && entity.getType().equals(getEntityType())){
            updateEntity(serverLevel, faction, entity.blockPosition(), false, ranks.get(0), entity);
        }
    }

    public Entity createEntity(ServerLevel level, Faction faction, BlockPos spawnBlockPos, boolean bannerHolder, FactionEntityRank rank, MobSpawnType spawnReason) {
        Entity entity;
        entity = getEntityType().create(level);
        if (entity == null) {
            return null;
        }
        entity.moveTo(spawnBlockPos.getX() + 0.5D, spawnBlockPos.getY() + 1.0D, spawnBlockPos.getZ() + 0.5D, entity.getYRot(), entity.getXRot());
        if (entity instanceof Mob mobEntity) {
            if (net.minecraftforge.common.ForgeHooks.canEntitySpawn(mobEntity, level, spawnBlockPos.getX(), spawnBlockPos.getY(), spawnBlockPos.getZ(), null, spawnReason) == -1) {
                return null;
            }
        }
        updateEntity(level, faction, spawnBlockPos, bannerHolder, rank, entity);

        level.addFreshEntityWithPassengers(entity.getRootVehicle());
        return entity;
    }

    private void updateEntity(ServerLevel level, Faction faction, BlockPos spawnBlockPos, boolean bannerHolder, FactionEntityRank rank, Entity entity) {
        if (entity instanceof Mob mobEntity) {
            if (bannerHolder) {
                faction.makeBannerHolder(mobEntity);
            }
            if(tagFirst) mergeTag(entity, this.getTag());
            if(shouldFinalizeSpawn)  mobEntity.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnBlockPos), MobSpawnType.EVENT, null, null);
            if(!tagFirst) mergeTag(entity, this.getTag());
            if(mobEntity.getNavigation() instanceof GroundPathNavigation groundPathNavigation) {
                groundPathNavigation.setCanOpenDoors(true);
            }
            mobEntity.setOnGround(true);
        }
        entity.getRootVehicle().getSelfAndPassengers()
                .forEach(stackedEntity -> {
                    if (stackedEntity instanceof Mob mob) {
                        FactionEntity cap = ((IFactionEntityDataHolder) mob).getOrCreateFactionEntityData();
                        cap.setFaction(faction);
                        cap.setFactionEntityType(this);
                        cap.getFaction().getBoostConfig().getMandatoryBoosts().forEach(boost -> boost.apply(mob));
                        cap.getFactionEntityType().getBoostConfig().getMandatoryBoosts().forEach(boost -> boost.apply(mob));
                        cap.setFactionEntityRank(rank);
                    }
                });
    }

    private void mergeTag(Entity entity, CompoundTag tag) {
        if(tag.isEmpty()) return;
        CompoundTag toLoad = new CompoundTag();
        entity.save(toLoad);
        toLoad = toLoad.merge(tag);
        entity.load(toLoad);
    }

    public CompoundTag save(CompoundTag compoundNbt) {
        return LegacyFactionEntityTypeLoader.saveStatic(this, compoundNbt);
    }

    public MobSpawnSettings.SpawnerData toSpawnerData() {
        if(spawnerData != null) return spawnerData;
        spawnerData = new MobSpawnSettings.SpawnerData(getEntityType(), weight, 1, 1);
        return spawnerData;
    }
}
