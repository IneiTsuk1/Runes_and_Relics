package net.IneiTsuki.regen.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.IneiTsuki.regen.screen.SpellInscriber.SpellInscriberScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SpellInscriberScreen extends HandledScreen<SpellInscriberScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("regen", "textures/gui/spell_inscriber.png");

    public SpellInscriberScreen(SpellInscriberScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 188;
        this.backgroundHeight = 208;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // Required setup
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();

        // Centered coordinates
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // Draw the main GUI section (left part of the texture)
        context.drawTexture(TEXTURE, x, y,
                0, 0,                        // u, v in texture
                backgroundWidth, backgroundHeight); // width of main gui

        //Draw the side panel (Assume its right next the main gui in texture)
        int sidePanelWidth = 68;
        int sidePanelHeight = 151;
        int sidePanelU = 188; // right after the main gui 189px over
        int sidePanelV = 0;

        //render the side panel to the right of the main gui
        context.drawTexture(TEXTURE,
                x + backgroundWidth, y,     // draw to screen to the right of the main Gui
                sidePanelU, sidePanelV,        // texture origin
                sidePanelWidth, sidePanelHeight // dimensions of side panel
        );

        // Draw the full texture
        //context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        this.titleY = 4;
        this.playerInventoryTitleX = (backgroundWidth - textRenderer.getWidth(playerInventoryTitle)) / 2;
        this.playerInventoryTitleY = 110;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
