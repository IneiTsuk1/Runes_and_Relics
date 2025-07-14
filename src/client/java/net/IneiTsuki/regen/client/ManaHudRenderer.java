package net.IneiTsuki.regen.client;

import net.IneiTsuki.regen.magic.components.ManaComponent;
import net.IneiTsuki.regen.magic.components.ModComponents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ManaHudRenderer implements HudRenderCallback {
    private static final Identifier MANA_BAR_TEXTURE = Identifier.of("regen", "textures/gui/mana_bar.png");

    private int lastMana = -1;
    private int displayTicksLeft = 0;
    private static final int DISPLAY_DURATION = 60; // 3 seconds at 20 ticks/sec

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        PlayerEntity player = client.player;
        ManaComponent mana = ModComponents.MANA.get(player);
        if (mana == null) return;

        int currentMana = mana.getMana();
        int maxMana = mana.getMaxMana();

        if (currentMana != lastMana) {
            lastMana = currentMana;
            displayTicksLeft = DISPLAY_DURATION;
        } else if (currentMana < maxMana) {
            displayTicksLeft = Math.max(displayTicksLeft, 1); // keep bar visible if not full
        } else {
            displayTicksLeft = Math.max(0, displayTicksLeft - 1);
        }

        if (displayTicksLeft <= 0) return; // Don't draw

        int barHeight = 9;
        int fullBarWidth = 48;
        int x = 10;
        int y = client.getWindow().getScaledHeight() - 20;

        int filledWidth = (int)((currentMana / (float) maxMana) * fullBarWidth);

        // Draw background
        drawContext.drawTexture(MANA_BAR_TEXTURE, x, y, 0, 0, fullBarWidth, barHeight);

        // Draw foreground
        if (filledWidth > 0) {
            drawContext.drawTexture(MANA_BAR_TEXTURE, x, y, 16, 0, filledWidth, barHeight);
        }

        // Optional: Draw text (or remove this line if you want a minimalist bar)
        drawContext.drawText(client.textRenderer, currentMana + " / " + maxMana, x + 2, y - 10, 0xFFFFFF, false);
    }
}
