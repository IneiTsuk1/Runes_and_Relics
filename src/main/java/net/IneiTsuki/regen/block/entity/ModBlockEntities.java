package net.IneiTsuki.regen.block.entity;

import net.IneiTsuki.regen.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ModBlockEntities {
    public static BlockEntityType<SpellInscriberBlockEntity> SPELL_INSCRIBER;

    public static void registerAll() {
        SPELL_INSCRIBER = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of("regen", "spell_inscriber"),
                FabricBlockEntityTypeBuilder.create(SpellInscriberBlockEntity::new, ModBlocks.SPELL_INSCRIBER_BLOCK).build(null)
        );
    }

}
