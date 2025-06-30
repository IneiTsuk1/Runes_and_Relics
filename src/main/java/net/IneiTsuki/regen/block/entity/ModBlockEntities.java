package net.IneiTsuki.regen.block.entity;

import net.IneiTsuki.regen.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Holds and registers custom {@link BlockEntityType} instances for the mod.
 */
public class ModBlockEntities {

    /**
     * The {@link SpellInscriberBlockEntity} type used by the Spell Inscriber block.
     */
    public static BlockEntityType<SpellInscriberBlockEntity> SPELL_INSCRIBER;

    /**
     * Registers all mod block entities with the game's registry.
     *
     * <p>Call this method during mod initialization to ensure block entities are available in-game.
     */
    public static void registerAll() {
        SPELL_INSCRIBER = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of("regen", "spell_inscriber"),
                FabricBlockEntityTypeBuilder
                        .create(SpellInscriberBlockEntity::new, ModBlocks.SPELL_INSCRIBER_BLOCK)
                        .build(null)
        );
    }
}
