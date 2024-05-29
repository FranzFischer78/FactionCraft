package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.boost.*;
import com.infamousmisadventures.factioncraft.boost.ai.DiggerBoost;
import com.infamousmisadventures.factioncraft.boost.ai.MeleeAttackBoost;
import com.infamousmisadventures.factioncraft.boost.ai.ShieldAIBoost;
import com.infamousmisadventures.factioncraft.boost.ai.ThrowPotionBoost;
import com.infamousmisadventures.factioncraft.platform.Services;

import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.registry.FCRegistries.BOOST_TYPE;
import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCBoostTypes {

    public static final Supplier<BoostType<NoBoost>> NO_BOOST = registerBoostType("no_boost", () -> new BoostType<>(NoBoost.CODEC));
    public static final Supplier<BoostType<AttributeBoost>> ATTRIBUTE = registerBoostType("attribute_boost", () -> new BoostType<>(AttributeBoost.CODEC));
    public static final Supplier<BoostType<WearArmorBoost>> WEAR_ARMOR = registerBoostType("wear_armor_boost", () -> new BoostType<>(WearArmorBoost.CODEC));
    public static final Supplier<BoostType<WearHandsBoost>> WEAR_HANDS = registerBoostType("wear_hands_boost", () -> new BoostType<>(WearHandsBoost.CODEC));
    public static final Supplier<BoostType<DaylightProtectionBoost>> DAYLIGHT_PROTECTION = registerBoostType("daylight_protection_boost", () -> new BoostType<>(DaylightProtectionBoost.CODEC));
    public static final Supplier<BoostType<MountBoost>> MOUNT = registerBoostType("mount_boost", () -> new BoostType<>(MountBoost.CODEC));
    public static final Supplier<BoostType<FactionMountBoost>> FACTION_MOUNT = registerBoostType("faction_mount_boost", () -> new BoostType<>(FactionMountBoost.CODEC));
    public static final Supplier<BoostType<MeleeAttackBoost>> MELEE_ATTACK = registerBoostType("melee_attack_boost", () -> new BoostType<>(MeleeAttackBoost.CODEC));
    public static final Supplier<BoostType<DiggerBoost>> DIGGER = registerBoostType("digger_boost", () -> new BoostType<>(DiggerBoost.CODEC));
    public static final Supplier<BoostType<ThrowPotionBoost>> THROW_POTION = registerBoostType("throw_potion_boost", () -> new BoostType<>(ThrowPotionBoost.CODEC));
    public static final Supplier<BoostType<ShieldAIBoost>> SHIELD_AI = registerBoostType("shield_ai_boost", () -> new BoostType<>(ShieldAIBoost.CODEC));
    public static final Supplier<BoostType<RoleBoost>> ROLE = registerBoostType("role_boost", () -> new BoostType<>(RoleBoost.CODEC));
    
    public static void register() {
    }

    private static <U extends Boost> Supplier<BoostType<U>> registerBoostType(String id, Supplier<BoostType<U>> attribSup) {
        return Services.REGISTRAR.registerObject(modLoc(id), attribSup, BOOST_TYPE);
    }
}
