package net.IneiTsuki.regen.screen;

import net.IneiTsuki.regen.block.entity.SpellInscriberBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * A custom slot implementation for the output slot of the Spell Inscriber.
 * Prevents item insertion and triggers crafting logic when items are taken.
 */
public class OutputSlot extends Slot {

    private final PlayerEntity player;
    private final SpellInscriberBlockEntity blockEntity;
    private boolean isShiftClick = false;

    /**
     * Creates a new OutputSlot instance.
     *
     * @param inventory   The inventory this slot belongs to.
     * @param index       The slot index in the inventory.
     * @param x           The x position on the screen.
     * @param y           The y position on the screen.
     * @param player      The player interacting with the slot.
     * @param blockEntity The SpellInscriber block entity backing this slot.
     */
    public OutputSlot(Inventory inventory, int index, int x, int y, PlayerEntity player, SpellInscriberBlockEntity blockEntity) {
        super(inventory, index, x, y);
        this.player = player;
        this.blockEntity = blockEntity;
    }

    /**
     * Prevents insertion into the output slot.
     *
     * @param stack The stack to be inserted.
     * @return false always, to disallow insertion.
     */
    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    /**
     * Allows the player to take items from this slot.
     *
     * @param playerEntity The player attempting to take items.
     * @return true always, to allow taking items.
     */
    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return true;
    }

    /**
     * Sets whether this interaction is a shift-click.
     * Called from the screen handler's quickMove method.
     *
     * @param shiftClick true if this is a shift-click interaction
     */
    public void setShiftClick(boolean shiftClick) {
        this.isShiftClick = shiftClick;
    }

    /**
     * Called when the player takes an item from the slot.
     * Triggers the crafting process in the SpellInscriberBlockEntity.
     * Supports shift-click crafting by checking the shift-click flag.
     *
     * @param player The player taking the item.
     * @param stack  The item stack taken.
     */
    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        super.onTakeItem(player, stack);

        if (blockEntity != null) {
            var world = blockEntity.getWorld();
            if (world != null && !world.isClient) {
                try {
                    if (isShiftClick) {
                        blockEntity.craftAllPossible();
                    } else {
                        blockEntity.craftSingle();
                    }
                    blockEntity.updateOutputSlot();
                } catch (Exception e) {
                    // Log error but don't crash
                    System.err.println("Error during crafting: " + e.getMessage());
                } finally {
                    // Reset shift-click flag
                    isShiftClick = false;
                }
            }
        }
    }
}
