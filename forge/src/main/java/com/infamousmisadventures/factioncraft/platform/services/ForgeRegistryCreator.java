package com.infamousmisadventures.factioncraft.platform.services;

import com.infamousmisadventures.factioncraft.registry.RegistrySyncer;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.FCConstants.MOD_ID;

public class ForgeRegistryCreator implements IRegistryCreator {

    private final List<RegistrySyncer<?>> syncers = new ArrayList<>();

    @Override
    public void setupRegistryCreator() {
    }

    public <P> Registry<P> createRegistry(Class<P> type, ResourceLocation registryName) {
        ResourceKey<Registry<P>> registryKey = ResourceKey.createRegistryKey(registryName);
        DeferredRegister<P> deferredRegister = DeferredRegister.create(registryKey, MOD_ID);
        ForgeRegistrar.addDeferredRegister(registryKey, deferredRegister);
        Supplier<IForgeRegistry<P>> iForgeRegistrySupplier = deferredRegister.makeRegistry(RegistryBuilder::new);
        MappedRegistry<P> registry = new MappedRegistry<>(registryKey, Lifecycle.stable());
        syncers.add(new RegistrySyncer<>(iForgeRegistrySupplier, registry));
        return registry;
    }

    public void syncRegistries() {
        syncers.forEach(RegistrySyncer::sync);
    }
}
