package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.blockentity.ReconstructBlockEntity;
import com.infamousmisadventures.factioncraft.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.registry.FCBlocks.RECONSTRUCT_BLOCK;
import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCBlockEntityTypes {

    public static final Supplier<BlockEntityType<ReconstructBlockEntity>> RECONSTRUCT_BLOCK_ENTITY = registerBlockEntity("reconstruct_block_entity",
            () -> BlockEntityType.Builder.of(ReconstructBlockEntity::new, RECONSTRUCT_BLOCK.get()).build(null));

    public static void register() {
    }

    private static Supplier<BlockEntityType<?>> registerBlockEntity(String id, Supplier<BlockEntityType<?>> attribSup) {
        return Services.REGISTRAR.registerObject(modLoc(id), attribSup, BuiltInRegistries.BLOCK_ENTITY_TYPE);
    }
}
