package net.IneiTsuki.regen.client;

import net.IneiTsuki.regen.client.screen.handlers.ModScreenHandlers;
import net.IneiTsuki.regen.client.screen.SpellInscriberScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

/**
 * Client-side initializer for the Regen mod.
 * Responsible for registering client-only components such as screen GUIs.
 */
public class RegenClient implements ClientModInitializer {

    /**
     * Called when the client mod is initialized.
     * Registers the custom screen handler to open the SpellInscriberScreen GUI.
     */
    @Override
    public void onInitializeClient() {
        ManaSyncClient.registerClientReceiver();

        HudRenderCallback.EVENT.register(new ManaHudRenderer());

        HandledScreens.register(ModScreenHandlers.SPELL_INSCRIBER, SpellInscriberScreen::new);
    }
}
