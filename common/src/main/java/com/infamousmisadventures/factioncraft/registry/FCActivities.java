package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.entity.ai.brain.sensor.FactionSpecificSensor;
import com.infamousmisadventures.factioncraft.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCActivities {

    public static final Supplier<Activity> FACTION_RAID = registerActivity("faction_raid");
    public static final Supplier<Activity> PRE_FACTION_RAID = registerActivity("faction_pre_raid");
    public static final Supplier<Activity> FACTION_RAIDER_PREP = registerActivity("faction_raider_prep");
    public static final Supplier<Activity> FACTION_RAIDER_VILLAGE = registerActivity("faction_raider_village");
    public static final Supplier<Activity> FACTION_PATROL = registerActivity("faction_patrol");
    public static final Supplier<Activity> DIG  = registerActivity("faction_craft_dig");

    public static void register() {
    }

    private static Supplier<Activity> registerActivity(String id) {
        return Services.REGISTRAR.registerObject(modLoc(id), () -> new Activity(id), BuiltInRegistries.ACTIVITY);
    }
}
