package com.infamousmisadventures.factioncraft.faction;

import com.infamousmisadventures.factioncraft.faction.entity.FactionEntityRank;
import com.infamousmisadventures.factioncraft.faction.entity.FactionEntityType;
import com.infamousmisadventures.factioncraft.faction.relations.FactionRelations;
import com.infamousmisadventures.factioncraft.faction.spawning.DominionSpawner;
import com.infamousmisadventures.factioncraft.level.saveddata.FactionData;
import com.infamousmisadventures.factioncraft.raid.config.FactionBattleConfigType;
import com.infamousmisadventures.factioncraft.raid.config.PlayerRaidConfigType;
import com.infamousmisadventures.factioncraft.raid.config.RaidConfigType;
import com.infamousmisadventures.factioncraft.raid.config.VillageRaidConfigType;
import com.infamousmisadventures.factioncraft.util.data.ResourceSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.infamousmisadventures.factioncraft.config.FactionCraftConfig.DISABLED_FACTIONS;
import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;
import static net.minecraft.world.level.Level.OVERWORLD;

public class Faction {
    public static List<RaidConfigType> DEFAULT_RAIDS = List.of(VillageRaidConfigType.DEFAULT, PlayerRaidConfigType.DEFAULT, FactionBattleConfigType.DEFAULT);
    public static final Faction DEFAULT = new Faction(new ResourceLocation("faction/default"), false, FactionType.MONSTER, new CompoundTag(), DEFAULT_RAIDS, FactionBoostConfig.DEFAULT, FactionRelations.DEFAULT, modLoc("default"), List.of(OVERWORLD.location()), ResourceSet.getEmpty(Registries.ENTITY_TYPE));
    public static final Faction GAIA = new Faction(new ResourceLocation("faction/gaia"), false, FactionType.GAIA, new CompoundTag(), new ArrayList<>(), FactionBoostConfig.DEFAULT, FactionRelations.DEFAULT, modLoc("default"), List.of(OVERWORLD.location()), ResourceSet.getEmpty(Registries.ENTITY_TYPE));
    public static final ResourceLocation VILLAGE_NAME = new ResourceLocation("faction/village");

    public static final Codec<Faction> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("name").forGetter(Faction::getName),
                    Codec.BOOL.optionalFieldOf("replace", false).forGetter(data -> data.replace),
                    FactionType.CODEC.optionalFieldOf("type", FactionType.MONSTER).forGetter(Faction::getFactionType),
                    CompoundTag.CODEC.fieldOf("banner").forGetter(Faction::getBanner),
                    RaidConfigType.CODEC.listOf().optionalFieldOf("raid_config", DEFAULT_RAIDS).forGetter(Faction::getRaidConfigs),
                    FactionBoostConfig.CODEC.optionalFieldOf("boosts", FactionBoostConfig.DEFAULT).forGetter(Faction::getBoostConfig),
                    FactionRelations.CODEC_OLD.optionalFieldOf("relations", FactionRelations.DEFAULT).forGetter(Faction::getRelations),
                    ResourceLocation.CODEC.optionalFieldOf("activation_advancement", modLoc("activation_advancement")).forGetter(Faction::getActivationAdvancement),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("home_dimensions", List.of(OVERWORLD.location())).forGetter(Faction::getHomeDimensions),
                    ResourceSet.getCodec(Registries.ENTITY_TYPE).optionalFieldOf("default_entities", ResourceSet.getEmpty(Registries.ENTITY_TYPE)).forGetter(data -> data.defaultEntities)
            ).apply(builder, Faction::new));

    private final ResourceLocation name;
    private final boolean replace;
    private final FactionType factionType;
    private final CompoundTag banner;
    private final List<RaidConfigType> raidConfigs;
    private final FactionBoostConfig boostConfig;
    private final FactionRelations relations;
    private List<FactionEntityType> entityTypes = new ArrayList<>();
    private final ResourceLocation activationAdvancement;
    private final List<ResourceLocation> homeDimensions;
    private final ResourceSet<EntityType<?>> defaultEntities;
    private final DominionSpawner dominionSpawner = new DominionSpawner(this);

    public Faction(ResourceLocation name, boolean replace, FactionType factionType, CompoundTag banner, List<RaidConfigType> raidConfigs, FactionBoostConfig boostConfig, FactionRelations relations, ResourceLocation activationAdvancement, List<ResourceLocation> homeDimensions, ResourceSet<EntityType<?>> defaultEntities) {
        this.name = name;
        this.replace = replace;
        this.factionType = factionType;
        this.banner = banner;
        this.raidConfigs = raidConfigs;
        this.boostConfig = boostConfig;
        this.relations = relations;
        this.activationAdvancement = activationAdvancement;
        this.homeDimensions = homeDimensions;
        this.defaultEntities = defaultEntities;
    }

    public ResourceLocation getName() {
        return name;
    }

    public boolean isReplace() {
        return replace;
    }

    public FactionType getFactionType() {
        return factionType;
    }

    public CompoundTag getBanner() {
        return banner;
    }

    public List<RaidConfigType> getRaidConfigs() {
        return raidConfigs;
    }

    public FactionBoostConfig getBoostConfig() {
        return boostConfig;
    }

    public FactionRelations getRelations() {
        return relations;
    }

    public List<FactionEntityType> getEntityTypes() {
        return entityTypes;
    }

    public ResourceLocation getActivationAdvancement() {
        return activationAdvancement;
    }

    public List<ResourceLocation> getHomeDimensions() {
        return homeDimensions;
    }

    public ResourceSet<EntityType<?>> getDefaultEntities() {
        return defaultEntities;
    }

    public boolean canRaid() {
        return !raidConfigs.isEmpty();
    }

    public List<Pair<FactionEntityType, Integer>> getWeightMap(){
        return entityTypes.stream().map(factionEntityType -> new Pair<>(factionEntityType, factionEntityType.getWeight())).toList();
    }

    public List<Pair<FactionEntityType, Integer>> getWeightMapForRank(FactionEntityRank rank){
        return entityTypes.stream().filter(factionEntityType -> factionEntityType.hasRank(rank)).map(factionEntityType -> new Pair<>(factionEntityType, factionEntityType.getWeight())).toList();
    }

    public Map<FactionEntityType, Integer> getWeightMap(EntityWeightMapProperties properties){
        return entityTypes.stream().filter(
                factionEntityType -> factionEntityType.canSpawnInWave(properties.getWave())
                && factionEntityType.hasRanks(properties.getAllowedRanks())
                && factionEntityType.canSpawnForOmen(properties.getOmen())
                && factionEntityType.canSpawnForBiome(properties.getBiome())
                && factionEntityType.canSpawnForYPos(properties.getBlockPos()))
                .collect(Collectors.toMap(Function.identity(), FactionEntityType::getWeight));
    }

    public ItemStack getBannerInstance() {
        ItemStack itemstack = ItemStack.of(banner);
        return itemstack;
    }

    public void makeBannerHolder(Mob mobEntity) {
        mobEntity.setItemSlot(EquipmentSlot.HEAD, getBannerInstance());
        mobEntity.setDropChance(EquipmentSlot.HEAD, 2.0F);
    }

    public void addEntityTypes(Collection<FactionEntityType> factionEntityTypes) {
        entityTypes = new ArrayList<>(entityTypes);
        entityTypes.addAll(factionEntityTypes);
    }

    public boolean isAllyOf(Faction entityFaction) {
        if (entityFaction == null)
            return false;
        return relations.isAllyOf(entityFaction);
    }

    public boolean isEnemyOf(Faction entityFaction) {
        if (entityFaction == null)
            return false;
        return relations.isEnemyOf(entityFaction);
    }

    public FactionData toFactionData() {
        return new FactionData(name, relations);
    }

    public boolean isActive() {
        return !DISABLED_FACTIONS.get().contains(name.toString());
    }

    public boolean isActive(Level level) {
        if(level instanceof ServerLevel serverLevel){
            Advancement advancement = serverLevel.getServer().getAdvancements().getAdvancement(getActivationAdvancement());
            return isActive() && (advancement == null || level.getServer().getPlayerList().getPlayers().stream().anyMatch(serverPlayerEntity -> serverPlayerEntity.getAdvancements().getOrStartProgress(advancement).isDone()));
        }
        return isActive();
    }

    public List<MobSpawnSettings.SpawnerData> getDominionSpawners(LevelAccessor level, BlockPos spawnBlockPos, int dominionAmount) {
        return dominionSpawner.GetSpawnerData(level, spawnBlockPos, dominionAmount);
    }

    public List<FactionEntityType> getSpawnableFactionEntityTypes(LevelAccessor level, BlockPos spawnBlockPos, int dominionAmount) {
        return dominionSpawner.GetSpawnableFactionEntityTypes(level, spawnBlockPos, dominionAmount);
    }
}
