package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.entity.ai.brain.sensor.FactionSpecificSensor;
import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.raid.Raid;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCMemoryModuleTypes {

    public static final Supplier<MemoryModuleType<GlobalPos>> RAID_WALK_TARGET = registerMemoryModuleType("raid_walk_target",
            () -> new MemoryModuleType<GlobalPos>(Optional.empty()));

    public static final Supplier<MemoryModuleType<LivingEntity>> NEAREST_VISIBLE_FACTION_ENEMY = registerMemoryModuleType("nearest_visible_faction_enemy",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final Supplier<MemoryModuleType<LivingEntity>> NEAREST_VISIBLE_FACTION_ALLY = registerMemoryModuleType("nearest_visible_faction_ally",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final Supplier<MemoryModuleType<LivingEntity>> NEAREST_VISIBLE_DAMAGED_FACTION_ALLY = registerMemoryModuleType("nearest_visible_damaged_faction_ally",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final Supplier<MemoryModuleType<List<GlobalPos>>> RAIDED_VILLAGE_POI = registerMemoryModuleType("raided_village_poi",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final Supplier<MemoryModuleType<Raid>> RAID = registerMemoryModuleType("faction_raid",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final Supplier<MemoryModuleType<Boolean>> PATROLLER = registerMemoryModuleType("patroller",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final Supplier<MemoryModuleType<Boolean>> IS_STUCK = registerMemoryModuleType("stuck",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static void register() {
    }

    private static <U> Supplier<MemoryModuleType<U>> registerMemoryModuleType(String id, Supplier<MemoryModuleType<U>> attribSup) {
        return Services.REGISTRAR.registerObject(modLoc(id), attribSup, BuiltInRegistries.MEMORY_MODULE_TYPE);
    }
}
