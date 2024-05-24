package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.faction.entity.FactionEntityType;
import com.infamousmisadventures.factioncraft.util.data.CodecJsonDataManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.infamousmisadventures.factioncraft.config.FactionCraftConfig.DISABLED_FACTIONS;
import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;
import static net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE;

public class FCFactionEntityTypes {
    public static final ResourceLocation RESOURCELOCATION = modLoc("faction_entity_type");

    public static final CodecJsonDataManager<FactionEntityType> FACTION_ENTITY_TYPE_DATA = new CodecJsonDataManager<>(RESOURCELOCATION, "faction_entity_type", FactionEntityType.CODEC);

    public static FactionEntityType getFactionEntityType(ResourceLocation factionResourceLocation){
        return getFactionEntityTypeData().getOrDefault(factionResourceLocation, FactionEntityType.DEFAULT);
    }

    public static boolean factionEntityTypeExists(ResourceLocation factionResourceLocation){
        return getFactionEntityTypeData().containsKey(factionResourceLocation);
    }

    public static Collection<ResourceLocation> factionEntityTypeKeys(){
        return getFactionEntityTypeData().keySet();
    }

    private static Map<ResourceLocation, FactionEntityType> getFactionEntityTypeData(){
        return FACTION_ENTITY_TYPE_DATA.getData().entrySet().stream().filter(entry -> !DISABLED_FACTIONS.get().contains(entry.getKey().toString())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<ResourceLocation, FactionEntityType> getFactionEntityTypeData(Faction faction){
        ResourceLocation key = FCFactions.getKey(faction);
        if(key == null) return new HashMap<>();
        return FACTION_ENTITY_TYPE_DATA.getData().entrySet().stream()
                .filter(entry -> isOfFaction(entry.getKey(), key))
                .filter(entry -> entityTypeExists(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static boolean entityTypeExists(FactionEntityType value) {
        return ENTITY_TYPE.containsKey(value.getEntityTypeName());
    }

    private static boolean isOfFaction(ResourceLocation entityType, ResourceLocation faction) {
        return entityType.getNamespace().equals(faction.getNamespace()) && entityType.getPath().startsWith(faction.getPath());
    }

    public static ResourceLocation getFactionEntityTypeKey(FactionEntityType factionEntityType) {
        return FACTION_ENTITY_TYPE_DATA.getData().entrySet().stream().filter(entry -> entry.getValue().equals(factionEntityType)).map(Map.Entry::getKey).findFirst().orElse(null);
    }
}
