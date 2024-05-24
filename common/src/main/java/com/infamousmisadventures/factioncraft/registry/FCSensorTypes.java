package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.entity.ai.brain.sensor.FactionSpecificSensor;
import com.infamousmisadventures.factioncraft.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCSensorTypes {

    public static final Supplier<SensorType<?>> FACTION_SENSOR = registerSensorType("faction_sensor",
            () -> new SensorType(FactionSpecificSensor::new));

    public static void register() {
    }

    private static Supplier<SensorType<?>> registerSensorType(String id, Supplier<SensorType<?>> attribSup) {
        return Services.REGISTRAR.registerObject(modLoc(id), attribSup, BuiltInRegistries.SENSOR_TYPE);
    }
}
