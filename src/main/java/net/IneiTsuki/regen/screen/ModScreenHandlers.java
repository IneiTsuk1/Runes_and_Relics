package net.IneiTsuki.regen.screen;

import net.IneiTsuki.regen.Regen;
import net.IneiTsuki.regen.client.screen.SpellInscriber.SpellInscriberScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

/**
 * Registers custom screen handlers for the mod.
 */
public class ModScreenHandlers {

    /**
     * Screen handler type for the Spell Inscriber GUI.
     */
    public static final ScreenHandlerType<SpellInscriberScreenHandler> SPELL_INSCRIBER =
            Registry.register(Registries.SCREEN_HANDLER,
                    Identifier.of(Regen.MOD_ID, "spell_inscriber"),
                    new ScreenHandlerType<>(SpellInscriberScreenHandler::new, null));

    /**
     * Registers all screen handlers for the mod.
     */
    public static void registerScreenHandlers() {
        Regen.LOGGER.info("Registering Screen Handlers for " + Regen.MOD_ID);
    }
}