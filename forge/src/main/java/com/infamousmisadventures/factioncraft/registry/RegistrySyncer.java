package com.infamousmisadventures.factioncraft.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Supplier;

public record RegistrySyncer<U> (Supplier<IForgeRegistry<U>> registry, Registry<U> mappedRegistry) {
    public void sync() {
        registry.get().forEach(o -> {
            ResourceLocation key = registry.get().getKey(o);
            Registry.register(mappedRegistry, key, o);
        });
    }
}
