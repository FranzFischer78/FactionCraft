package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.effect.FactionBadOmenEffect;
import com.infamousmisadventures.factioncraft.entity.ai.brain.sensor.FactionSpecificSensor;
import com.infamousmisadventures.factioncraft.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCMobEffects {

    public static final Supplier<MobEffect> FACTION_BAD_OMEN = registerMobEffect("faction_bad_omen",
            () -> new FactionBadOmenEffect(MobEffectCategory.BENEFICIAL, 10044730));

    public static void register() {
    }

    private static Supplier<MobEffect> registerMobEffect(String id, Supplier<MobEffect> attribSup) {
        return Services.REGISTRAR.registerObject(modLoc(id), attribSup, BuiltInRegistries.MOB_EFFECT);
    }
}
