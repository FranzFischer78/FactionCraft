package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.item.OminousBottleItem;
import com.infamousmisadventures.factioncraft.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCItems {

    public static final Supplier<Item> OMINOUS_BOTTLE = registerBlock("ominous_bottle", () ->
            new OminousBottleItem(new Item.Properties().stacksTo(1)));

    public static void register() {
    }

    private static Supplier<Item> registerBlock(String id, Supplier<Item> attribSup) {
        return Services.REGISTRAR.registerObject(modLoc(id), attribSup, BuiltInRegistries.ITEM);
    }
}
