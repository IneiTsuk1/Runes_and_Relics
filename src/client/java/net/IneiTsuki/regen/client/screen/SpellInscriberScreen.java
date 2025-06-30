package net.IneiTsuki.regen.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.IneiTsuki.regen.screen.SpellInscriber.SpellInscriberScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * GUI screen for the Spell Inscriber block.
 * Displays the main crafting interface with custom background and side panel.
 */
public class SpellInscriberScreen extends HandledScreen<SpellInscriberScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("regen", "textures/gui/spell_inscriber.png");

    /**
     * Constructs the SpellInscriberScreen.
     *
     * @param handler the screen handler for interaction with server and inventory
     * @param inventory the player inventory to render
     * @param title the title text to display at the top
     */
    public SpellInscriberScreen(SpellInscriberScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 188;
        this.backgroundHeight = 208;
    }

    /**
     * Draws the background of the GUI, including the main panel and side panel.
     *
     * @param context the rendering context
     * @param delta frame delta time
     * @param mouseX mouse X position
     * @param mouseY mouse Y position
     */
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // Setup shader and texture
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();

        // Calculate top-left corner for centered rendering
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // Draw main GUI texture (left part)
        context.drawTexture(TEXTURE, x, y,
                0, 0,                        // texture coordinates u, v
                backgroundWidth, backgroundHeight);

        // Draw side panel to the right of main GUI
        int sidePanelWidth = 68;
        int sidePanelHeight = 151;
        int sidePanelU = 188; // X offset in texture for side panel
        int sidePanelV = 0;

        context.drawTexture(TEXTURE,
                x + backgroundWidth, y,
                sidePanelU, sidePanelV,
                sidePanelWidth, sidePanelHeight);
    }

    /**
     * Initializes the GUI, positioning title and player inventory labels.
     */
    @Override
    protected void init() {
        super.init();
        // Center the screen title at the top
        this.titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        this.titleY = 4;

        // Center the player inventory title label
        this.playerInventoryTitleX = (backgroundWidth - textRenderer.getWidth(playerInventoryTitle)) / 2;
        this.playerInventoryTitleY = 110;
    }

    /**
     * Renders the screen including background, slots, and tooltips.
     *
     * @param context the rendering context
     * @param mouseX current mouse X position
     * @param mouseY current mouse Y position
     * @param delta frame delta time
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
