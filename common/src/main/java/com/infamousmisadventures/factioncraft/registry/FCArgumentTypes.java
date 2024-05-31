package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.commands.arguments.FactionArgument;
import com.infamousmisadventures.factioncraft.commands.arguments.FactionEntitySummonArgument;
import com.infamousmisadventures.factioncraft.platform.Services;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCArgumentTypes {

    public static final Supplier<SingletonArgumentInfo<FactionArgument>> FACTION = registerArgumentType("faction",
            () -> SingletonArgumentInfo.contextFree(FactionArgument::new), FactionArgument.class);

    public static final Supplier<SingletonArgumentInfo<FactionEntitySummonArgument>> FACTION_ENTITY_TYPE = registerArgumentType("faction_entity_type",
            () -> SingletonArgumentInfo.contextFree(FactionEntitySummonArgument::new), FactionEntitySummonArgument.class);
    
    public static void register() {
    }

    private static <U extends ArgumentTypeInfo<?, ?>> Supplier<U> registerArgumentType(String id, Supplier<U> attribSup, Class<? extends ArgumentType> clazz) {
        Supplier<U> uSupplier = Services.REGISTRAR.registerObject(modLoc(id), attribSup, BuiltInRegistries.COMMAND_ARGUMENT_TYPE);
        ArgumentTypeInfos.BY_CLASS.put(clazz, uSupplier.get());
        return uSupplier;
    }
}
