package net.IneiTsuki.regen.datagen.loot;

import net.IneiTsuki.regen.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

/**
 * Data provider for generating block loot tables.
 *
 * <p>This class defines what items blocks drop when broken. It is used by the Fabric data generator
 * to automatically produce JSON files under the {@code data/regen/loot_tables/blocks} directory.
 */
public class ModLootTableProvider extends FabricBlockLootTableProvider {

    /**
     * Constructs a new block loot table provider.
     *
     * @param dataOutput     The data output target used to write generated data.
     * @param registryLookup A future providing access to the registry.
     */
    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    /**
     * Generates loot table entries for blocks.
     *
     * <p>This method is called automatically by the data generator.
     */
    @Override
    public void generate() {
        // Make the Spell Inscriber block drop itself when broken
        addDrop(ModBlocks.SPELL_INSCRIBER_BLOCK);
    }
}
