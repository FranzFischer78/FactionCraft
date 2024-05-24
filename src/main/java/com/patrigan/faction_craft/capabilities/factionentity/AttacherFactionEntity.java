package com.infamousmisadventures.factioncraft.capabilities.factionentity;

import com.infamousmisadventures.factioncraft.capabilities.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import com.infamousmisadventures.factioncraft.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

import static com.infamousmisadventures.factioncraft.FactionCraft.MODID;

public class AttacherFactionEntity {

    private static class FactionEntityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

        public static final ResourceLocation IDENTIFIER = new ResourceLocation(MODID, "faction_entity");
        private final FactionEntity backend;
        private final LazyOptional<FactionEntity> optionalData;

        public FactionEntityProvider(LivingEntity entity) {
            backend = new FactionEntity(entity);
            optionalData = LazyOptional.of(() -> backend);
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
            return ModCapabilities.FACTION_ENTITY_CAPABILITY.orEmpty(cap, this.optionalData);
        }

        @Override
        public CompoundTag serializeNBT() {
            return this.backend.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.backend.deserializeNBT(nbt);
        }
    }

    // attach only to Mob entities
    public static void attach(final AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof LivingEntity) {
            final AttacherFactionEntity.FactionEntityProvider provider = new AttacherFactionEntity.FactionEntityProvider((LivingEntity) entity);
            event.addCapability(AttacherFactionEntity.FactionEntityProvider.IDENTIFIER, provider);
        }
    }
}
