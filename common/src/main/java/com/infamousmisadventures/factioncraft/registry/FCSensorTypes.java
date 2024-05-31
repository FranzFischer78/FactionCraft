package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.entity.ai.brain.sensor.FactionSpecificSensor;
import com.infamousmisadventures.factioncraft.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCSensorTypes {

    public static final Supplier<SensorType<FactionSpecificSensor>> FACTION_SENSOR = registerSensorType("faction_sensor",
            () -> new SensorType(FactionSpecificSensor::new));

    public static void register() {
    }

    private static <U extends Sensor<?>> Supplier<SensorType<U>> registerSensorType(String id, Supplier<SensorType<U>> attribSup) {
        return Services.REGISTRAR.registerObject(modLoc(id), attribSup, BuiltInRegistries.SENSOR_TYPE);
    }
}
