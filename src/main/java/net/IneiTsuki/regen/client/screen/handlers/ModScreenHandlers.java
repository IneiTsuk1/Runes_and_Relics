package net.IneiTsuki.regen.client.screen.handlers;

import net.IneiTsuki.regen.client.screen.SpellInscriber.SpellInscriberScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

/**
 * Registers and holds references to custom ScreenHandler types used by the mod.
 */
public class ModScreenHandlers {

    /** ScreenHandlerType instance for the Spell Inscriber GUI. */
    public static ScreenHandlerType<SpellInscriberScreenHandler> SPELL_INSCRIBER;

    /**
     * Registers all custom ScreenHandler types with Minecraft's registry.
     * Should be called during mod initialization.
     */
    public static void registerAll() {
        SPELL_INSCRIBER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of("regen", "spell_inscriber"),
                new ScreenHandlerType<>(
                        SpellInscriberScreenHandler::new,
                        FeatureFlags.DEFAULT_ENABLED_FEATURES
                )
        );
    }
}
