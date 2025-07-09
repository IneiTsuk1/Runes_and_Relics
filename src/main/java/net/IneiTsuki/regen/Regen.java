package net.IneiTsuki.regen;

import net.IneiTsuki.regen.block.ModBlocks;
import net.IneiTsuki.regen.block.entity.ModBlockEntities;
import net.IneiTsuki.regen.item.ModItems;
import net.IneiTsuki.regen.magic.item.ManaRegenTask;
import net.IneiTsuki.regen.magic.network.ManaSyncPacket;
import net.IneiTsuki.regen.magic.components.ManaComponent;
import net.IneiTsuki.regen.magic.components.ModComponents;
import net.IneiTsuki.regen.magic.effect.active.ActiveSpellTracker;
import net.IneiTsuki.regen.magic.core.scheduler.TickScheduler;
import net.IneiTsuki.regen.magic.item.MagicScrollItems;
import net.IneiTsuki.regen.client.screen.handlers.ModScreenHandlers;
import net.IneiTsuki.regen.recipe.ModRecipes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod initializer class for the Regen mod.
 * Responsible for registering blocks, items, block entities, recipes, screen handlers, and magic scrolls.
 * Also sets up creative item groups.
 */
public class Regen implements ModInitializer {

    /** The mod ID string */
    public static final String MOD_ID = "regen";

    /** Logger instance for the mod */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Called when the mod is initialized.
     * Registers mod content and sets up creative tabs.
     */
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Regen Mod");

        ModBlocks.registerModBlocks();
        ModItems.registerModItems();
        ModBlockEntities.registerAll();
        ModRecipes.registerRecipes();
        ModScreenHandlers.registerAll();
        MagicScrollItems.registerItems();

        // In your main mod class
        PayloadTypeRegistry.playS2C().register(ManaSyncPacket.ID, ManaSyncPacket.CODEC);


        // Add magic scrolls and related items to the TOOLS creative tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(MagicScrollItems::addItemsToItemGroup);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            ManaComponent mana = ModComponents.MANA.get(player);
            ManaRegenTask task = new ManaRegenTask(mana, 2, 20); // regen 2 mana every 20 ticks (1 second)
            TickScheduler.schedule(20, task);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            TickScheduler.tick();
            ActiveSpellTracker.tick(server.getOverworld());
        });

        LOGGER.info("Regen Mod initialized successfully!");
    }

    /**
     * Helper method to create namespaced identifiers with this mod's ID.
     *
     * @param path The path part of the identifier.
     * @return Identifier with mod ID as namespace and the given path.
     */
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
