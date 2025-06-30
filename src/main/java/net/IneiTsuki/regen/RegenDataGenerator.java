package net.IneiTsuki.regen;

import net.IneiTsuki.regen.datagen.ModBlockTagProvider;
import net.IneiTsuki.regen.datagen.ModLanguageProvider;
import net.IneiTsuki.regen.datagen.ModLootTableProvider;
import net.IneiTsuki.regen.datagen.ModModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class RegenDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModBlockTagProvider::new);
        pack.addProvider(ModLootTableProvider::new);
        pack.addProvider(ModModelProvider::new);
        pack.addProvider(ModLanguageProvider::english);
        pack.addProvider(ModLanguageProvider::spanish);
        pack.addProvider(ModLanguageProvider::french);
        pack.addProvider(ModLanguageProvider::german);
       // pack.addProvider(AdvancedMagicScrollModelProvider::new);
    }
}
