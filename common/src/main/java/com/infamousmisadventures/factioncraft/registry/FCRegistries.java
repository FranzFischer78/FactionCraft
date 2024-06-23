package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.boost.BoostType;
import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.raid.target.RaidConfigBaseType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCRegistries {
    public static Registry<BoostType> BOOST_TYPE = createRegistry(BoostType.class, modLoc("boost_type"));
    public static Registry<RaidConfigBaseType> RAID_CONFIG_BASE_TYPE = createRegistry(RaidConfigBaseType.class, modLoc("raid_config_base_type"));

    public static void register() {
    }

    private static <P> Registry<P> createRegistry(Class<P> classType, ResourceLocation registryLocation) {
        return Services.REGISTRY_CREATOR.createRegistry(classType, registryLocation);
    }

}
