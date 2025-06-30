package net.IneiTsuki.regen.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Entry point for data generation in the Regen mod.
 * This class registers various data providers such as block tags,
 * item models, loot tables, and language files to generate JSON resources.
 */
public class RegenDataGenerator implements DataGeneratorEntrypoint {

    /**
     * Called when the data generator is initialized.
     * This is where you add your data providers to generate mod assets.
     *
     * @param fabricDataGenerator The Fabric data generator instance.
     */
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        // Add your data providers here, e.g.:
        // pack.addProvider(ModBlockTagProvider::new);
        // pack.addProvider(ModLootTableProvider::new);
        // pack.addProvider(ModModelProvider::new);
        // pack.addProvider(ModLanguageProvider::english);
        // etc.
    }
}
