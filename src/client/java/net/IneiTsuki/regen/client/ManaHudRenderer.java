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

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        PlayerEntity player = client.player;
        ManaComponent mana = ModComponents.MANA.get(player);

        int currentMana = mana.getMana();
        int maxMana = mana.getMaxMana();

        int barWidth = 80;
        int barHeight = 10;
        int x = 10;
        int y = 10;

        int filledWidth = (int)((currentMana / (float) maxMana) * barWidth);

        // Draw background (full bar frame)
        drawContext.drawTexture(MANA_BAR_TEXTURE, x, y, 0, 0, barWidth, barHeight);

        // Draw filled mana section
        if (filledWidth > 0) {
            drawContext.drawTexture(MANA_BAR_TEXTURE, x, y, 0, barHeight, filledWidth, barHeight);
        }

        // Draw mana text
        drawContext.drawText(client.textRenderer, currentMana + " / " + maxMana, x + 2, y + 2, 0xFFFFFF, false);
    }
}
