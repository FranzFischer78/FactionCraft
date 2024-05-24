package com.infamousmisadventures.factioncraft.util;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

import static com.infamousmisadventures.factioncraft.FCConstants.MOD_ID;

public class ResourceLocationHelper {

    public static ResourceLocation modLoc(String path) {
        return new ResourceLocation(MOD_ID, path.toLowerCase(Locale.ROOT));
    }
}
