package net.IneiTsuki.regen.datagen.tags;

import net.IneiTsuki.regen.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

/**
 * Data provider class for generating block tag JSON files.
 *
 * <p>This class is used in the mod's data generation process to specify block tag entries,
 * such as which blocks can be mined with which tools.
 *
 * <p>Run the datagen task to generate the resulting tag files in the `datagen` output folder.
 */
public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {

    /**
     * Constructs a new block tag provider for this mod.
     *
     * @param output            The fabric data output for writing generated files.
     * @param registriesFuture  A future for registry lookup used during tag resolution.
     */
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    /**
     * Configures the block tags for this mod.
     *
     * <p>This method is automatically called during data generation.
     * Here you define which of your blocks go into which tags.
     *
     * @param wrapperLookup The registry wrapper context used for tag resolution.
     */
    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        // Add the Spell Inscriber block to the "mineable/axe" tag
        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(ModBlocks.SPELL_INSCRIBER_BLOCK);
    }
}
