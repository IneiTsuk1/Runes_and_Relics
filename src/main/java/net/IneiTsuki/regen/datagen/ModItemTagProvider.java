package net.IneiTsuki.regen.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Data provider for generating item tag JSON files.
 *
 * <p>This class is part of the data generation pipeline and is responsible for adding your mod's items
 * to Minecraft or custom-defined item tags (e.g., tools, ingredients, etc.).
 *
 * <p>Use the {@link #configure(RegistryWrapper.WrapperLookup)} method to define tag membership.
 */
public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    /**
     * Constructs a new item tag provider for this mod.
     *
     * @param output             The output target for generated data files.
     * @param completableFuture  A future providing access to registry information.
     * @param blockTagProvider   The associated block tag provider, used for copying tags from blocks to items (can be null).
     */
    public ModItemTagProvider(FabricDataOutput output,
                              CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture,
                              @Nullable FabricTagProvider.BlockTagProvider blockTagProvider) {
        super(output, completableFuture, blockTagProvider);
    }

    /**
     * Configures item tags by adding entries to them.
     *
     * <p>This method is called automatically during data generation.
     *
     * @param wrapperLookup Provides access to registries and tag data.
     */
    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        // Add items to tags here using getOrCreateTagBuilder(tag).add(item)...
    }
}
