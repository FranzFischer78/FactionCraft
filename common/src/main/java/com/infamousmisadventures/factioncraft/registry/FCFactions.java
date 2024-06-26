package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.boost.Boost;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.faction.FactionBoostConfig;
import com.infamousmisadventures.factioncraft.faction.FactionRaidConfig;
import com.infamousmisadventures.factioncraft.faction.FactionType;
import com.infamousmisadventures.factioncraft.faction.relations.FactionRelations;
import com.infamousmisadventures.factioncraft.level.saveddata.PlayerFaction;
import com.infamousmisadventures.factioncraft.level.saveddata.PlayerFactions;
import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.raid.target.RaidConfigType;
import com.infamousmisadventures.factioncraft.util.GeneralUtils;
import com.infamousmisadventures.factioncraft.util.data.MergeableCodecDataManager;
import com.infamousmisadventures.factioncraft.util.data.ResourceSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.infamousmisadventures.factioncraft.FCConstants.MOD_ID;
import static com.infamousmisadventures.factioncraft.faction.relations.FactionRelation.ENEMY_THRESHOLD;
import static com.infamousmisadventures.factioncraft.faction.relations.FactionRelation.NEUTRAL;
import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;
import static net.minecraft.core.registries.Registries.ENTITY_TYPE;

public class FCFactions {


    public static final MergeableCodecDataManager<Faction, Faction> FACTION_DATA = new MergeableCodecDataManager<>("faction", Faction.CODEC, FCFactions::factionMerger);
    public static final Map<UUID, Faction> PLAYER_FACTIONS = new HashMap<>();

    public static Faction factionMerger(List<Faction> raws){
        ResourceLocation name = null;
        FactionType factionType = null;
        CompoundTag banner = null;
        List<RaidConfigType> raidConfigs = new ArrayList<>();
        FactionBoostConfig boostConfig = null;
        FactionRelations factionRelations = null;
        ResourceLocation activationAdvancement = null;
        List<ResourceLocation> homeDimensions = new ArrayList<>();
        ResourceSet<EntityType<?>> defaultEntities = new ResourceSet<>(ENTITY_TYPE, new ArrayList<>());
        for (Faction raw : raws) {
            if (raw.isReplace()) {
                factionType = raw.getFactionType();
                banner = raw.getBanner();
                name = raw.getName();
                raidConfigs = raw.getRaidConfigs();
                boostConfig = null;
                factionRelations = null;
                homeDimensions.clear();
                activationAdvancement = raw.getActivationAdvancement();
            }
            if(factionType == null){
                factionType = raw.getFactionType();
            }
            if(banner == null){
                banner = raw.getBanner();
            }
            if(name == null){
                name = raw.getName();
            }
            raidConfigs.addAll(raw.getRaidConfigs());
            if(activationAdvancement == null){
                activationAdvancement = raw.getActivationAdvancement();
            }
            if(boostConfig == null){
                boostConfig = raw.getBoostConfig();
            }else{
                List<ResourceLocation> mandatoryBoosts = Stream.concat(boostConfig.getMandatoryResourceLocations().stream(), raw.getBoostConfig().getMandatoryResourceLocations().stream()).collect(Collectors.toList());
                List<ResourceLocation> whitelistBoosts = Stream.concat(boostConfig.getWhitelistResourceLocations().stream(), raw.getBoostConfig().getWhitelistResourceLocations().stream()).collect(Collectors.toList());
                List<ResourceLocation> blacklistBoosts = Stream.concat(boostConfig.getBlacklistResourceLocations().stream(), raw.getBoostConfig().getBlacklistResourceLocations().stream()).collect(Collectors.toList());
                List<Pair<ResourceLocation, Boost.Rarity>> rarityOverridesLocations = Stream.concat(boostConfig.getRarityOverridesLocations().stream(), raw.getBoostConfig().getRarityOverridesLocations().stream()).collect(Collectors.toList());
                boostConfig = new FactionBoostConfig(boostConfig.getBoostDistributionType(), mandatoryBoosts, whitelistBoosts, blacklistBoosts, rarityOverridesLocations);
            }
            if(factionRelations == null){
                factionRelations = raw.getRelations();
            }else{
                List<ResourceLocation> allies = Stream.concat(factionRelations.getAllies().stream(), raw.getRelations().getAllies().stream()).collect(Collectors.toList());
                List<ResourceLocation> enemies = Stream.concat(factionRelations.getEnemies().stream(), raw.getRelations().getEnemies().stream()).collect(Collectors.toList());
                factionRelations = new FactionRelations(allies, enemies);
            }
            homeDimensions.addAll(raw.getHomeDimensions());
            defaultEntities = defaultEntities.merge(raw.getDefaultEntities());
        }
        return new Faction(name,false, factionType, banner, raidConfigs, boostConfig, factionRelations, activationAdvancement, homeDimensions, defaultEntities);
    }


    public static Faction getFaction(ResourceLocation factionResourceLocation){
        return getFactionData().getOrDefault(factionResourceLocation, Faction.GAIA);
    }

    public static boolean factionExists(ResourceLocation factionResourceLocation){
        return getFactionData().containsKey(factionResourceLocation);
    }

    public static Collection<ResourceLocation> factionKeys(){
        return getFactionData().keySet();
    }

    public static Map<ResourceLocation, Faction> getFactionData(){
        return FACTION_DATA.getData().entrySet().stream().filter(entry -> entry.getValue().isActive()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Collection<Faction> getActiveFactions(Level level){
        return FACTION_DATA.getData().values().stream().filter(faction -> faction.isActive(level)).collect(Collectors.toList());
    }

    public static Faction getRandomFaction(Level level, RandomSource random, Predicate<Faction> predicate) {
        List<Faction> possibleFactions = getActiveFactions(level).stream().filter(predicate).collect(Collectors.toList());
        return GeneralUtils.getRandomItem(possibleFactions, random);
    }


    public static Faction getRandomFactionWithEnemies(ServerLevel level, RandomSource random, Predicate<Faction> predicate) {
        List<Faction> possibleFactions = getActiveFactions(level).stream().filter(predicate).collect(Collectors.toList());
        return GeneralUtils.getRandomItem(possibleFactions.stream().filter(faction -> !faction.getRelations().getEnemies().isEmpty()).collect(Collectors.toList()), random);
    }

    public static ResourceLocation getKey(Faction faction){
        if(Faction.GAIA.equals(faction)) return Faction.GAIA.getName();
        return FACTION_DATA.getData().entrySet().stream().filter(entry -> entry.getValue().equals(faction)).map(Map.Entry::getKey).findFirst().orElse(null);
    }


    public static Collection<ResourceLocation> getEnemyFactionKeysOf(Faction faction) {
        if(faction == null) return Collections.emptyList();
        return getFactionData().entrySet().stream()
                .filter(entry -> entry.getValue().getRelations().getEnemies().contains(getKey(faction)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static void addPlayerFaction(Faction faction) {
        if (!factionExists(faction.getName())) {
            FACTION_DATA.addData(faction.getName(), faction);
        }
    }

    public static Faction createPlayerFaction(Player player){
        Faction faction = new Faction(new ResourceLocation(MOD_ID, "player/" + player.getName().getString().toLowerCase()), false, FactionType.PLAYER, new CompoundTag(), FactionRaidConfig.PLAYER, FactionBoostConfig.DEFAULT, FactionRelations.DEFAULT, modLoc("default"), new ArrayList<>(), ResourceSet.getEmpty(ENTITY_TYPE));
        for (Faction faction1 : getFactionData().values()) {
            if(!faction.getRelations().getEnemies().contains(getKey(faction))){
                if(faction1.getFactionType().equals(FactionType.MONSTER)) {
                    faction.getRelations().adjustRelation(faction1, ENEMY_THRESHOLD);
                    faction1.getRelations().adjustRelation(faction, ENEMY_THRESHOLD);
                }
            }else {
                faction.getRelations().adjustRelation(faction1, NEUTRAL);
                faction1.getRelations().adjustRelation(faction, NEUTRAL);
            }
        }
        addPlayerFaction(faction);
        return faction;
    }

    public static void reloadPlayerFactions() {
        MinecraftServer currentServer = Services.PLATFORM.getCurrentServer();
        if(currentServer == null) return;
        PlayerFactions playerFactions = PlayerFactions.getOrCreate(currentServer.getLevel(ServerLevel.OVERWORLD));
        playerFactions.getPlayerFactions().forEach((uuid, playerFaction) -> reloadPlayerFaction(playerFaction));
    }

    private static void reloadPlayerFaction(PlayerFaction playerFaction) {
        Faction faction = playerFaction.getFaction();
        FCFactions.addPlayerFaction(faction);
        for (Faction faction1 : getFactionData().values()) {
            if(!faction.getRelations().getEnemies().contains(getKey(faction))){
                if(faction1.getFactionType().equals(FactionType.MONSTER)) {
                    faction.getRelations().adjustRelation(faction1, ENEMY_THRESHOLD);
                    faction1.getRelations().adjustRelation(faction, ENEMY_THRESHOLD);
                }
            }else {
                faction.getRelations().adjustRelation(faction1, NEUTRAL);
                faction1.getRelations().adjustRelation(faction, NEUTRAL);
            }
        }
    }
}
