package net.IneiTsuki.regen;

import net.IneiTsuki.regen.datagen.tags.ModBlockTagProvider;
import net.IneiTsuki.regen.datagen.language.ModLanguageProvider;
import net.IneiTsuki.regen.datagen.loot.ModLootTableProvider;
import net.IneiTsuki.regen.datagen.model.ModModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Data generator entrypoint for the Regen mod.
 * Registers providers for generating block tags, loot tables, models, and language files.
 * Supports multiple languages.
 */
public class RegenDataGenerator implements DataGeneratorEntrypoint {

    /**
     * Called to initialize data generation for the mod.
     * Adds data providers to the FabricDataGenerator pack.
     *
     * @param fabricDataGenerator the Fabric data generator instance
     */
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        // Add providers for various data types
        pack.addProvider(ModBlockTagProvider::new);
        pack.addProvider(ModLootTableProvider::new);
        pack.addProvider(ModModelProvider::new);

        // Register language providers for supported languages
        pack.addProvider(ModLanguageProvider::english);
        pack.addProvider(ModLanguageProvider::spanish);
        pack.addProvider(ModLanguageProvider::french);
        pack.addProvider(ModLanguageProvider::german);

        // Uncomment if you add advanced scroll model provider in the future
        // pack.addProvider(AdvancedMagicScrollModelProvider::new);
    }
}
